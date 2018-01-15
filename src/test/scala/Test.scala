import java.text.SimpleDateFormat
import java.util.Date

import io.github.yuemenglong.http.HttpClient
import io.github.yuemenglong.json.JSON
import io.github.yuemenglong.json.lang.JsonDate

/**
  * Created by <yuemenglong@126.com> on 2018/1/15.
  */

class Bar {
  @JsonDate
  var date: Date = _
  var open: Double = _
  var high: Double = _
  var low: Double = _
  var close: Double = _
}

object Test {
  def main(args: Array[String]): Unit = {
    val client = new HttpClient
    //    val url = "http://q.stock.sohu.com/hisHq?code=zs_000001&start=20000504&end=20151215&stat=1&order=D&period=d&callback=historySearchHandler&rt=jsonp&r=0.8391495715053367&0.9677250558488026"
    val url = "http://q.stock.sohu.com/hisHq?code=zs_000001&start=20000504&end=20151215&stat=1&order=D&period=d&rt=json&r=0.8391495715053367&0.9677250558488026"
    client.setCookieString("vjuids=4a08c4925.158f270a575.0.4889c22306691; sohutag=8HsmeSc5NSwmcyc5NywmYjc5NSwmYSc5NSwmZjc5NCwmZyc5NCwmbjc5NCwmaSc5NCwmdyc5NCwmaCc5NCwmYyc5NCwmZSc5NCwmbSc5NCwmdCc5NH0; vjlast=1481536218.1495169557.11; SUV=1612101609570991; BIZ_MyLBS=cn_600641%2C%u4E07%u4E1A%u4F01%u4E1A%7C; IPLOC=CN4101")
    val body = client.httpGet(url).getBody
    val jo = JSON.parse(body)
    val bars = jo.asArr().getObj(0).getArr("hq").array.map(_.asArr().array).map(arr => {
      val bar = new Bar
      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      bar.date = sdf.parse(arr(0).asStr())
      bar.open = arr(1).asStr().toDouble
      bar.close = arr(2).asStr().toDouble
      bar.low = arr(5).asStr().toDouble
      bar.high = arr(6).asStr().toDouble
      bar
    })
    bars.map(JSON.stringify(_)).foreach(println)
  }
}
