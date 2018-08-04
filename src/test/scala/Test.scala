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
  var price: Double = _

  var preMoney: Double = _
  var preStockVol: Double = _
  var preInput: Double = _

  def preStock: Double = this.preStockVol * this.price

  def preValue: Double = this.preMoney + this.preStock

  var postMoney: Double = _
  var postStockVol: Double = _
  var postInput: Double = _

  def postStock: Double = this.postStockVol * this.price

  def postValue: Double = this.postMoney + this.postStock

  def input: Double = this.postInput - this.preInput

  def buyVol: Double = this.postStockVol - this.preStockVol

  def buyCost: Double = this.buyVol * this.price

  def percent: Double = (this.postValue - this.postInput) / this.postInput * 100

  var ratio: Double = _

  override def toString: String = {
    def round(double: Double): Double = {
      (double * 100).toInt / 100.0
    }

    def dateStr(date: Date): String = {
      new SimpleDateFormat("yyyy-MM-dd").format(date)
    }

    (Array(dateStr(this.date)) ++
      Array(this.price, this.ratio, this.buyVol, this.buyCost,
        this.preMoney, this.preStock, this.postMoney, this.postStock,
        this.postValue, this.postInput, this.percent
      ).map(round)).mkString(",")
  }
}

object Test {
  def monthBars(bars: Array[Bar]): Array[Bar] = {
    bars.groupBy(b => s"${b.date.getYear}-${b.date.getMonth}").map(p => p._2.sortBy(_.date)).map(a => a(0)).toArray
      .sortBy(_.date)
  }

  def fn0(bar: Bar): Double = {
    val ret = 1
    ret / (1 + ret)
  }

  def fn1(bar: Bar): Double = {
    Math.pow(0.1, (bar.open - bar.ma) / bar.ma)
  }

  def fn11(bar: Bar): Double = {
    Math.pow(0.05, (bar.open - bar.ma) / bar.ma)
  }

  def fn2(bar: Bar): Double = {
    val r = (bar.open - bar.ma) / bar.ma
    val ret = Math.pow(0.33, r) - 0.5
    ret match {
      case _ if ret < 0 => 0
      case _ if ret > 1 => 1
      case _ => ret
    }
    //      case _ if r >= 0 => Math.sqrt(1 - r * r) / 2
    //    }
    //    ret
  }

  def main(args: Array[String]): Unit = {
    val cookie = "SUV=1712312255354743; vjuids=-69d27717.160f48d85e7.0.0019abdc8a4f; gidinf=x099980109ee0d7c33ae3e458000d078cd4448b5a44b; beans_mz_userid=T1fVe08TKg37; vjlast=1515931404.1520602056.12; mut=zz.go.smuid; _smuid=ALRTtZe2bXDcTduYsUFUI1; _smuid_type=2; IPLOC=CN4100; beans_dmp=%7B%22admaster%22%3A1532783425%2C%22shunfei%22%3A1532783425%2C%22reachmax%22%3A1532783425%2C%22lingji%22%3A1532783425%2C%22yoyi%22%3A1532783425%2C%22ipinyou%22%3A1532783425%2C%22ipinyou_admaster%22%3A1532783425%2C%22miaozhen%22%3A1532783425%2C%22diantong%22%3A1532783425%2C%22huayang%22%3A1532783425%7D; t=1532783423591"
    val client = new HttpClient
    val start = "20000101"
    val end = "20180101"
    //    val stock = "cn_600029"
    //    val stock = "zs_000016"
    val stock = "zs_000001"
    //    val url = "http://q.stock.sohu.com/hisHq?code=zs_000001&start=20000504&end=20151215&stat=1&order=D&period=d&callback=historySearchHandler&rt=jsonp&r=0.8391495715053367&0.9677250558488026"
    val url = s"http://q.stock.sohu.com/hisHq?code=$stock&start=$start&end=$end&stat=1&order=D&period=d&rt=json&r=0.8391495715053367&0.9677250558488026"
    client.setCookieString(cookie)
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
    println(bars.length)

//    val cycle = 260
//    bars.zipWithIndex.foreach { case (bar, i) =>
//      i match {
//        case 0 => bar.ma = bar.open
//        case n if n >= cycle => bar.ma = (bars(i - 1).ma * cycle - bars(i - cycle).open + bar.open) / cycle.toDouble
//        case n if 0 < n && n < cycle => bar.ma = (bars(i - 1).ma * (n - 1) + bar.open) / n
//      }
//    }
//    //    bars.map(JSON.stringify(_)).foreach(println)
//
//    //noinspection SimplifyBooleanMatch
//    val base = 1000
//    val arr = monthBars(bars.filter(b => b.date.after(new Date(110, 0, 1)))).foldLeft(ArrayBuffer[Record]())((arr, bar) => {
//      val ratio = fn11(bar)
//      val price = bar.open
//      var record = new Record
//      arr.lastOption match {
//        case Some(last) =>
//          record.preMoney = last.postMoney + base * 2
//          record.preStockVol = last.postStockVol
//          record.preInput = last.postInput
//        case None =>
//          record.preMoney = base * 2
//          record.preStockVol = 0
//          record.preInput = 0
//      }
//      record.date = bar.date
//      record.price = price
//      record.ratio = ratio
//      val input = base * ratio
//      val buy = input / price
//      record.postInput = record.preInput + input
//      record.postMoney = record.preMoney - input
//      record.postStockVol = record.preStockVol + buy
//      //      record.postInput = record.preInput + input
//      //      val currentValue = record.preValue + input
//      //      record.postMoney = currentValue * (1 - ratio)
//      //      record.postStockVol = currentValue * ratio / price
//      arr += record
//      arr
//    })
//
//    //    println("时间 买入 价格 均价 持仓 市值 成本 盈利 盈利百分点")
//    arr.foreach(println)
  }
}
