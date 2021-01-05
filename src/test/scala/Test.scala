import io.github.yuemenglong.http.HttpClient

object Test {
  def main(args: Array[String]): Unit = {
    val url = "http://api.map.baidu.com/directionlite/v1/walking?origin=40.058058890354786,116.31273215795211&destination=40.0505203789219,116.28131841426817&ak=noCcAZ6z8Lns27dqGcbsUuxHw9SgOPTP"
    val client = new HttpClient
    val res = client.httpGet(url)
    println(res.getBody)
  }
}
