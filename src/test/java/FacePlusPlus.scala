import java.io.File

import io.github.yuemenglong.http.HttpClient
import io.github.yuemenglong.json.JSON

/**
  * Created by <yuemenglong@126.com> on 2017/10/16.
  */


object FacePlusPlus {

  def detect(): Unit = {
    val path = Thread.currentThread().getContextClassLoader.getResource("test4.jpg").getFile
    println(path)

    val client = new HttpClient
    val formData = Map[String, Any](
      "api_key" -> "uqngbdsbwX9CsbqPeObfwzzlaUJpPDJC",
      "api_secret" -> "Q5kEO5lhl32wvb3mMhY0AIu90nEAob1o",
      "image_file" -> new File(path),
      "return_landmark" -> 1,
      "return_attributes" -> "gender,age,smiling,headpose,facequality,blur,eyestatus,emotion,ethnicity,beauty,mouthstatus,eyegaze,skinstatus",
    )
    val res = client.httpForm("https://api-cn.faceplusplus.com/facepp/v3/detect", formData)
    println(res.getBody)
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
    detect()
    //    3.to(5).foreach(i => {
    //      (i + 1).to(6).foreach(j => {
    //        val path1 = Thread.currentThread().getContextClassLoader.getResource(s"test$i.jpg").getFile
    //        val path2 = Thread.currentThread().getContextClassLoader.getResource(s"test$j.jpg").getFile
    //        println(i, j, compare(path1, path2))
    //      })
    //    })
  }

}
