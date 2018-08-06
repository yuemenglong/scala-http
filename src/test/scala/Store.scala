import java.text.SimpleDateFormat
import java.util.Date

import io.github.yuemenglong.http.HttpClient
import io.github.yuemenglong.json.JSON
import io.github.yuemenglong.json.lang.JsonDate

/**
  * Created by <yuemenglong@126.com> on 2018/8/5.
  */
class Bar {
  @JsonDate
  var date: String = _
  var open: Double = _
  var high: Double = _
  var low: Double = _
  var close: Double = _
  var diff: Double = _
  var percent: Double = _

  override def toString: String = f"${date}, ${open}%.2f, ${high}%.2f, ${low}%.2f, ${close}%.2f"
}

object Store {
  val cookie = "SUV=1712312255354743; vjuids=-69d27717.160f48d85e7.0.0019abdc8a4f; gidinf=x099980109ee0d7c33ae3e458000d078cd4448b5a44b; beans_mz_userid=T1fVe08TKg37; vjlast=1515931404.1520602056.12; mut=zz.go.smuid; _smuid=ALRTtZe2bXDcTduYsUFUI1; _smuid_type=2; IPLOC=CN4100; beans_dmp=%7B%22admaster%22%3A1532783425%2C%22shunfei%22%3A1532783425%2C%22reachmax%22%3A1532783425%2C%22lingji%22%3A1532783425%2C%22yoyi%22%3A1532783425%2C%22ipinyou%22%3A1532783425%2C%22ipinyou_admaster%22%3A1532783425%2C%22miaozhen%22%3A1532783425%2C%22diantong%22%3A1532783425%2C%22huayang%22%3A1532783425%7D; t=1532783423591"

  def nowDate: String = {
    new SimpleDateFormat("yyyyMMdd").format(new Date)
  }

  def fetch(code: String = "zs_000001", start: String = "20000101", end: String = nowDate): Array[Bar] = {
    val client = new HttpClient
    //    val start = "20000101"
    //    val end = "20180804"
    //    val stock = "cn_600029"
    //    val stock = "zs_000016"
    //    val code = "zs_000001"
    //    val url = "http://q.stock.sohu.com/hisHq?code=zs_000001&start=20000504&end=20151215&stat=1&order=D&period=d&callback=historySearchHandler&rt=jsonp&r=0.8391495715053367&0.9677250558488026"
    val url = s"http://q.stock.sohu.com/hisHq?code=$code&start=$start&end=$end&stat=1&order=D&period=d&rt=json&r=0.8391495715053367&0.9677250558488026"
    client.setCookieString(cookie)
    val body = client.httpGet(url).getBody
    val jo = JSON.parse(body)
    jo.asArr().getObj(0).getArr("hq").array.map(_.asArr().array).map(arr => {
      val bar = new Bar
      //      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      bar.date = arr(0).asStr() //sdf.parse(arr(0).asStr())
      bar.open = arr(1).asStr().toDouble
      bar.close = arr(2).asStr().toDouble
      bar.diff = arr(3).asStr().toDouble
      bar.percent = arr(4).asStr().replace("%", "").toDouble
      bar.low = arr(5).asStr().toDouble
      bar.high = arr(6).asStr().toDouble
      bar
    }).sortBy(_.date)
  }
}
