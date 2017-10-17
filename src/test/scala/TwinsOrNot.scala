import java.io.File

import io.github.yuemenglong.http.HttpClient
import io.github.yuemenglong.json.JSON

/**
  * Created by <yuemenglong@126.com> on 2017/9/11.
  */
object TwinsOrNot {
  def uploadFile(path: String, lr: String): String = {
    val client = new HttpClient
    val file = TwinsOrNot.getClass.getClassLoader.getResource(path).getFile
    val url = s"https://www.twinsornot.net/Home/AnalyzeOneImage?isTest=False&fileid=$lr"
    val res = client.httpForm(url, Map(s"processedImg$lr" -> new File(file)))
    println(res)
    val json = res.getBody.replaceAll("\\\\\"", "\"").replaceAll("(\"$)|(^\")", "")
    val jo = JSON.parse(json).asObj()
    jo.getArr("Faces").array(0).asObj().getStr("faceId")
  }

  def similar(left: String, right: String): Int = {
    val client = new HttpClient
    val url = s"https://www.twinsornot.net/Home/HowSimilar"
    val res = client.httpForm(url, Map("leftFaceID" -> left, "rightFaceID" -> right))
    println(res)
    val json = res.getBody.replaceAll("\\\\\"", "\"").replaceAll("(\"$)|(^\")", "")
    val jo = JSON.parse(json).asObj()
    jo.getInt("Score")
  }

  def main(args: Array[String]): Unit = {
    val left = uploadFile("test.jpg", "L")
    val right = uploadFile("test2.jpg", "R")
    val score = similar(left, right)
    println(score)
  }
}
