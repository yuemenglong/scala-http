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
  var ma: Double = _
}

class Record {
  @JsonDate
  var date: Date = _

  var 买入: Double = _
  var 比例: Double = _

  var 价格: Double = _
  var 持仓: Double = _
  var 成本: Double = _

  def 市值: Double = this.价格 * this.持仓

  def 盈利: Double = this.市值 - this.成本

  def 均价: Double = this.成本 / this.持仓

  def 盈利百分点: Double = this.盈利 / this.成本 * 100

  override def toString: String = {
    def round(double: Double): Double = {
      (double * 100).toInt / 100.0
    }

    def dateStr(date: Date): String = {
      new SimpleDateFormat("yyyy-MM-dd").format(date)
    }

    s"${dateStr(this.date)} ${round(this.买入)} ${round(this.价格)} ${round(this.均价)}" +
      s" ${round(this.持仓)} ${round(this.市值)} ${round(this.成本)} ${round(this.盈利)} ${round(this.盈利百分点)}"
  }
}

object Test {
  def monthBars(bars: Array[Bar]): Array[Bar] = {
    bars.groupBy(b => s"${b.date.getYear}-${b.date.getMonth}").map(p => p._2.sortBy(_.date)).map(a => a(0)).toArray
      .sortBy(_.date)
  }

  def fn0(bar: Bar): Double = {
    1
  }

  def fn1(bar: Bar): Double = {
    Math.pow(0.1, (bar.open - bar.ma) / bar.ma)
  }

  def fn2(bar: Bar): Double = {
    val r = (bar.open - bar.ma) / bar.ma
    -Math.pow(2 * r, 3) + 1
  }


  def main(args: Array[String]): Unit = {
    val client = new HttpClient
    val start = "20000101"
    val end = "20180101"
    //    val stock = "cn_600029"
    val stock = "zs_000016"
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

    val cycle = 260
    bars.zipWithIndex.foreach { case (bar, i) =>
      i match {
        case 0 => bar.ma = bar.open
        case n if n >= cycle => bar.ma = (bars(i - 1).ma * cycle - bars(i - cycle).open + bar.open) / cycle.toDouble
        case n if 0 < n && n < cycle => bar.ma = (bars(i - 1).ma * (n - 1) + bar.open) / n
      }
    }
    //    bars.map(JSON.stringify(_)).foreach(println)

    //noinspection SimplifyBooleanMatch
    val arr = monthBars(bars.filter(b => b.date.after(new Date(110, 0, 1)))).foldLeft(ArrayBuffer[Record]())((arr, bar) => {
      val price = bar.open
      val ratio = fn0(bar)
      var record = new Record
      record.date = bar.date
      record.价格 = price
      record.比例 = ratio
      record.买入 = ratio match {
        case x if x >= 0 => 1000 * ratio / price
        case x if x < 0 => arr.nonEmpty match {
          case true => ratio match {
            case `x` if x >= -1 => arr.last.持仓 * ratio
            case `x` if x < -1 => arr.last.持仓 * -1
          }
          case false => 0
        }
      }
      if (arr.nonEmpty) {
        record.成本 = arr.last.成本 + record.买入 * price
        record.持仓 = arr.last.持仓 + record.买入
      } else {
        record.成本 = record.买入 * price
        record.持仓 = record.买入
      }
      //      record.ratio = ratio
      //      record.price = bar.open
      //      record.input = ratio match {
      //        case x if x >= 0 => 1000 * ratio
      //        case x if x < 0 => arr.nonEmpty match {
      //          case true => ratio match {
      //            case `x` if x >= -1 => arr.last.totalVol * ratio * record.price
      //            case `x` if x < -1 => arr.last.totalVol * -1 * record.price
      //          }
      //          case false => 0
      //        }
      //      }
      //      record.vol = record.input / record.price
      //      if (arr.nonEmpty) {
      //        record.totalInput = arr.last.totalInput + record.input
      //        record.totalVol = arr.last.totalVol + record.vol
      //      } else {
      //        record.totalInput = record.input
      //        record.totalVol = record.vol
      //      }
      //
      //      record.totalValue = record.totalVol * record.price
      //      record.avgPrice = record.totalInput / record.totalVol
      //      record.profit = (record.price - record.avgPrice) * record.totalVol
      //      record.profitPercent = (record.price - record.avgPrice) / record.avgPrice * 100.0
      arr += record
      arr
    })

    println("时间 买入 价格 均价 持仓 市值 成本 盈利 盈利百分点")
    arr.foreach(println)
  }
}
