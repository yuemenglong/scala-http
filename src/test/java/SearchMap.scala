import java.io.FileOutputStream

import io.github.yuemenglong.http.{HttpClient, HttpKit}
import io.github.yuemenglong.json.JSON
import io.github.yuemenglong.json.parse.JsonObj

import scala.io.{Source, StdIn}

/**
  * Created by <yuemenglong@126.com> on 2017/9/11.
  */

object Query {
  val PAGESIZE = 20
  val geo0 = "113.481768|34.638128|113.976152|34.862107"

  val p0 = "113.657687,34.745902"
  val plu = "113.529113,34.840916"
  val pru = "113.83261,34.82429"
  val pld = "113.572715,34.67591"
  val prd = "113.781455,34.672805"

  def getGeoObj(p1: String, p2: String): String = {
    val Array(x1, y1) = p1.split(",")
    val Array(x2, y2) = p2.split(",")
    val left = Math.min(x1.toDouble, x2.toDouble)
    val right = Math.max(x1.toDouble, x2.toDouble)
    val top = Math.min(y1.toDouble, y2.toDouble)
    val bottom = Math.max(y1.toDouble, y2.toDouble)
    s"$left|$top|$right|$bottom"
  }
}

case class Pair(p1: String, p2: String) {
  def getGeoObj: String = Query.getGeoObj(p1, p2)
}


case class Query(keywords: String, pagenum: Int = 1,
                 geoobj: String = Query.getGeoObj(Query.plu, Query.p0)) {
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
  //  val geoobj = "113.481768|34.638128|113.976152|34.862107"
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
        //        Thread.sleep(1000)
        ret = query(q)
        succ = true
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          StdIn.readLine()
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

  def crawl(keywords: String, pair: Pair): CrawlResult = {
    var currentPage = 1
    val resList = Stream.continually({
      val q = Query(keywords, currentPage, pair.getGeoObj)
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

  def crawl(keywords: String, pairs: Array[Pair]): CrawlResult = {
    val list = pairs.map(p => {
      crawl(keywords, p)
    }).flatMap(_.list)
      .groupBy(l => s"${l.latitude},${l.longitude}")
      .map(_._2(0)).toArray
    CrawlResult(list.length, keywords, list)
  }

  def generagePair(p: String, width: Double, height: Double,
                   gx: Int, gy: Int, h: Int, v: Int): Array[Pair] = {
    val Array(px, py) = p.split(",").map(_.toDouble)
    val (x0, y0) = (px + gx * width, py + gy * height)
    (0 until h).flatMap(i => {
      (0 until v).map(j => {
        val (x1, y1) = (x0 + width * i, y0 + height * j)
        val (x2, y2) = (x0 + width * (i + 1), y0 + height * (j + 1))
        val s1 = s"$x1,$y1"
        val s2 = s"$x2,$y2"
        Pair(s1, s2)
      })
    }).toArray
  }

  def main(args: Array[String]): Unit = {
    //"113.647817,34.761839", "113.681806,34.738355")
    val ps = generagePair(Query.p0, 0.03, 0.02, -3, -3, 6, 6)
    val res = crawl("小区", ps)
    println(res.list.length)
    //    res.list.map(JSON.stringify(_)).foreach(println)
    val filePath = "location.js"
    val fs = new FileOutputStream(filePath)
    val js = "window.locs=" + JSON.stringify(res.list)
    fs.write(js.getBytes)
    fs.close()

    //    val res = query(Query("加油站"))
    //    println(res.total)
    //    res.list.map(JSON.stringify).foreach(println)
  }
}
