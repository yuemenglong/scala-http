import scala.collection.mutable.ArrayBuffer

/**
  * Created by <yuemenglong@126.com> on 2018/1/15.
  */

object RecordContext {
  var input: Double = 10000

  var records: ArrayBuffer[Record] = new ArrayBuffer[Record]()

  def buy(bar: Bar, vol: Double) {
    if (money >= vol * bar.close) {
      records += Record(bar.date, bar.close, vol)
    } else if (money > 0) {
      val v = money / bar.close
      records += Record(bar.date, bar.close, v)
    }
  }

  def sell(bar: Bar, vol: Double): Unit = {
    records += Record(bar.date, bar.close, -vol)
  }

  def money: Double = input - stock

  def stock: Double = records.map(r => r.price * r.vol).sum

  def vol: Double = records.map(_.vol).sum

  def avg: Double = stock / vol

  def stock(close: Double): Double = close * vol

  def total(close: Double): Double = money + stock(close)

  def profit(close: Double): Double = stock(close) * 100 / stock - 100
}

case class Record(date: String, price: Double, vol: Double)

object Limits {
  var limits: Array[(Double, Double)] = _

  def init(high: Double, low: Double, level: Int, ratio: Double): Unit = {
    val ratios = (1 to level).map(i => {
      Math.pow(ratio, i)
    })
    val sum = ratios.sum
    val fixed = ratios.map(n => {
      n / sum
    })
    val lines = (1 to level).map(i => {
      high - (high - low) / level * i
    })
    limits = lines.zip(fixed).toArray
        limits.foreach(println)
  }

  def cross(prev: Bar, bar: Bar): (Double, Double) =
    limits.reverse.find { case (line, _) =>
      (line - prev.close) * (line - bar.close) < 0
    }.orNull
}

object Test {
  def calcLimits(high: Double, low: Double, level: Int, ratio: Double): Array[(Double, Double)] = {
    val ratios = (1 to level).map(i => {
      Math.pow(ratio, i)
    })
    val sum = ratios.sum
    val fixed = ratios.map(n => {
      n / sum
    })
    val lines = (1 to level).map(i => {
      high - (high - low) / level * i
    })
    lines.zip(fixed).toArray
  }

  def println(ss: Any*): Unit = {
    Predef.println(toString(ss: _*))
  }

  def toString(ss: Any*): String = {
    ss.map {
      case d: Double => f"${d}%.2f"
      case s => s.toString
    }.mkString(",")
  }

  def main(args: Array[String]): Unit = {
    Limits.init(3000, 1600, 16, 1.4)

    val bars = Store.fetch(start = "20090901")
    val prevBars = bars.drop(1) ++ Array(bars.last)
    var curLine = 0.0
    //    val sb = bars.takeRight(750).sortBy(_.close)
    //    print(sb(sb.length / 2).close)
    bars.zip(prevBars).foreach { case (prev, bar) =>
      Limits.cross(prev, bar) match {
        case null =>
        case (line, ratio) => {
          if (curLine != line) {
            val vol = RecordContext.input * ratio / bar.close
            RecordContext.buy(bar, vol)
            //            println(bar.date, line, bar.close, ratio * 100,
            //              RecordContext.money, RecordContext.total(bar.close), RecordContext.avg)
            curLine = line
          }
        }
      }
      println(bar.date, bar.close,
        RecordContext.money, RecordContext.total(bar.close),
        RecordContext.avg, RecordContext.profit(bar.close))
    }
  }
}
