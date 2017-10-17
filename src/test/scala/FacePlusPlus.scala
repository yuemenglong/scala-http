import java.io.File

import io.github.yuemenglong.http.HttpClient
import io.github.yuemenglong.json.JSON

/**
  * Created by <yuemenglong@126.com> on 2017/10/16.
  */

//(3,4,85.2)
//(3,5,86.097)
//(3,6,86.243)
//(4,5,75.451)
//(4,6,77.188)
//(5,6,86.138)
//(3,7,42.078)
//(8,7,83.747)
//(6,7,44.265)

object FacePlusPlus {

  def detect(path: String): String = {
    val client = new HttpClient
    val formData = Map[String, Any](
      "api_key" -> "uqngbdsbwX9CsbqPeObfwzzlaUJpPDJC",
      "api_secret" -> "Q5kEO5lhl32wvb3mMhY0AIu90nEAob1o",
      "image_file" -> new File(path),
      "return_landmark" -> 1,
      "return_attributes" -> "gender,age,smiling,headpose,facequality,blur,eyestatus,emotion,ethnicity,beauty,mouthstatus,eyegaze,skinstatus",
    )
    val res = client.httpForm("https://api-cn.faceplusplus.com/facepp/v3/detect", formData)
    res.getBody
  }

  def compare(path1: String, path2: String): Double = {
    val client = new HttpClient
    val formData = Map[String, Any](
      "api_key" -> "uqngbdsbwX9CsbqPeObfwzzlaUJpPDJC",
      "api_secret" -> "Q5kEO5lhl32wvb3mMhY0AIu90nEAob1o",
      "image_file1" -> new File(path1),
      "image_file2" -> new File(path2),
    )
    val res = client.httpForm("https://api-cn.faceplusplus.com/facepp/v3/compare", formData)
    JSON.parse(res.getBody).asObj().getDouble("confidence")
  }

  def main(args: Array[String]): Unit = {
    //    3.to(5).foreach(i => {
    //      (i + 1).to(6).foreach(j => {
    //        val path1 = Thread.currentThread().getContextClassLoader.getResource(s"test$i.jpg").getFile
    //        val path2 = Thread.currentThread().getContextClassLoader.getResource(s"test$j.jpg").getFile
    //        println(i, j, compare(path1, path2))
    //      })
    //    })
    //    val i = 6
    //    val j = 7
    //    val path1 = Thread.currentThread().getContextClassLoader.getResource(s"test$i.jpg").getFile
    //    val path2 = Thread.currentThread().getContextClassLoader.getResource(s"test$j.jpg").getFile
    //    println(i, j, compare(path1, path2))
  }

}
