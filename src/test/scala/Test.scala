import java.text.SimpleDateFormat
import java.util.Date

import io.github.yuemenglong.http.HttpClient
import io.github.yuemenglong.json.JSON
import io.github.yuemenglong.json.lang.JsonDate

import scala.collection.mutable.ArrayBuffer

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
  var gap: Double = _
  var percent: Double = _
}

class Record {
  @JsonDate
  var date: Date = _
  var price: Double = _
  var ratio: Double = _
  var input: Double = _
  var vol: Double = _
  var totalVol: Double = _
  var totalValue: Double = _
  var totalInput: Double = _
  var avgPrice: Double = _

  override def toString: String = {
    def round(double: Double): Double = {
      (double * 100).toInt / 100.0
    }

    val dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date)
    s"$dateStr ${round(input)} ${round(totalInput)} ${round(price)} ${round(avgPrice)}"
  }
}

object Test {
  def monthBars(bars: Array[Bar]): Array[Bar] = {
    bars.groupBy(b => s"${b.date.getYear}-${b.date.getMonth}").map(p => p._2.sortBy(_.date)).map(a => a(0)).toArray
      .sortBy(_.date)
  }

  def main(args: Array[String]): Unit = {
    val client = new HttpClient
    val start = "20150501"
    val end = "20180101"
    val stock = "cn_600029"
    //    val stock = "zs_000002"
    //    val url = "http://q.stock.sohu.com/hisHq?code=zs_000001&start=20000504&end=20151215&stat=1&order=D&period=d&callback=historySearchHandler&rt=jsonp&r=0.8391495715053367&0.9677250558488026"
    val url = s"http://q.stock.sohu.com/hisHq?code=$stock&start=$start&end=$end&stat=1&order=D&period=d&rt=json&r=0.8391495715053367&0.9677250558488026"
    client.setCookieString("vjuids=4a08c4925.158f270a575.0.4889c22306691; sohutag=8HsmeSc5NSwmcyc5NywmYjc5NSwmYSc5NSwmZjc5NCwmZyc5NCwmbjc5NCwmaSc5NCwmdyc5NCwmaCc5NCwmYyc5NCwmZSc5NCwmbSc5NCwmdCc5NH0; vjlast=1481536218.1495169557.11; SUV=1612101609570991; BIZ_MyLBS=cn_600641%2C%u4E07%u4E1A%u4F01%u4E1A%7C; IPLOC=CN4101")
    val body = client.httpGet(url).getBody
    val jo = JSON.parse(body)
    val bars = jo.asArr().getObj(0).getArr("hq").array.map(_.asArr().array).map(arr => {
      val bar = new Bar
      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      bar.date = sdf.parse(arr(0).asStr())
      bar.open = arr(1).asStr().toDouble
      bar.close = arr(2).asStr().toDouble
      bar.gap = arr(3).asStr().toDouble
      bar.percent = arr(4).asStr().replace("%", "").toDouble
      bar.low = arr(5).asStr().toDouble
      bar.high = arr(6).asStr().toDouble
      bar
    }).sortBy(_.date)
    //    bars.map(JSON.stringify(_)).foreach(println)

    val ma = 6.3
    val base = 0.1
    val arr = monthBars(bars).foldLeft(ArrayBuffer[Record]())((arr, bar) => {
      val ratio = Math.pow(base, (bar.open - ma) / ma)
      var record = new Record
      record.date = bar.date
      record.input = 1000 * ratio
      record.price = bar.open
      record.vol = record.input / record.price
      if (arr.nonEmpty) {
        record.totalInput = arr.last.totalInput + record.input
        record.totalVol = arr.last.totalVol + record.vol
      } else {
        record.totalInput = record.input
        record.totalVol = record.vol
      }
      record.totalValue = record.totalVol * record.price
      record.avgPrice = record.totalInput / record.totalVol
      arr += record
      arr
    })

    def format(date: Date): String = {
      new SimpleDateFormat("yyyy-MM-dd").format(date)
    }

    arr.foreach(println)
  }
}
