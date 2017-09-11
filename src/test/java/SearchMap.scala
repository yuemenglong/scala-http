import java.io.FileOutputStream

import io.github.yuemenglong.http.{HttpClient, HttpKit}
import io.github.yuemenglong.json.JSON
import io.github.yuemenglong.json.parse.JsonObj

import scala.io.Source

/**
  * Created by <yuemenglong@126.com> on 2017/9/11.
  */

object Query {
  val PAGESIZE = 20
}

case class Query(keywords: String, pagenum: Int = 1) {
  val query_type = "TQUERY"
  val pagesiz: Int = Query.PAGESIZE
  //  val pagenum = 1
  val qii = true
  val cluster_state = 5
  val need_utd = true
  val utd_sceneid = 1000
  val div = "PC1000"
  val addr_poi_merge = true
  val is_classify = true
  val zoom = 13
  val city = 410100
  //  val geoobj = "113.598463|34.701454|113.845655|34.813434"
  val geoobj = "113.481768|34.638128|113.976152|34.862107"
  //  val keywords = "加油站"
}

class Location(jo: JsonObj) {
  val name: String = jo.getStr("name")
  val address: String = jo.getStr("address")
  val latitude: String = jo.getStr("latitude")
  val longitude: String = jo.getStr("longitude")
}

case class QueryResult(total: Int, query: Query, list: Array[Location])

case class CrawlResult(total: Int, keywords: String, list: Array[Location])

object SearchMap {

  def tryQuery(q: Query): QueryResult = {
    var retry = 0
    var succ = false
    var ret: QueryResult = null
    var ex: Throwable = null
    while (retry < 3 && !succ) {
      try {
        Thread.sleep(1000)
        ret = query(q)
        succ = true
      } catch {
        case e: Throwable =>
          retry += 1
          ex = e
      }
    }
    if (ret == null) {
      throw ex
    } else {
      ret
    }
  }

  def query(query: Query): QueryResult = {
    val client = new HttpClient
    val postfix = HttpKit.encodeObject(query)
    val url = s"http://gaode.com/service/poiInfo?$postfix"
    val res = client.httpGet(url)
    println(s"Get: $url")
    println(res.getBody)
    val jo = JSON.parse(res.getBody).asObj()
    val total = jo.getObj("data")
      .getInt("total")
      .intValue()
    val list = jo.getObj("data")
      .getArr("poi_list") match {
      case null => Array[Location]()
      case arr => arr.array.map(_.asObj())
        .map(new Location(_))
    }
    QueryResult(total, query, list)
  }

  def crawl(keywords: String): CrawlResult = {
    var currentPage = 1
    val resList = Stream.continually({
      val q = Query(keywords, currentPage)
      currentPage += 1
      tryQuery(q)
    }).takeWhile(res => {
      println(currentPage - 1, res.list.length, res.total)
      res.list.length > 0
    })
    val total = resList.head.total
    val list = resList.flatMap(_.list).toArray
    CrawlResult(total, keywords, list)
  }

  def main(args: Array[String]): Unit = {
    val res = crawl("加油站")
    //    println(res.list.length, res.total)
    //    res.list.map(JSON.stringify(_)).foreach(println)
    val filePath = Thread.currentThread().getContextClassLoader
      .getResource("location.json").getFile
    val fs = new FileOutputStream(filePath)
    val js = "window.locs=" + JSON.stringify(res.list)
    fs.write(js.getBytes)
    fs.close()

    //    val res = query(Query("加油站"))
    //    println(res.total)
    //    res.list.map(JSON.stringify).foreach(println)
  }
}
