import java.io.File

import io.github.yuemenglong.http.HttpClient

/**
  * Created by <yuemenglong@126.com> on 2017/10/16.
  */


object FacePlusPlus {

  def detect(): Unit = {
    val path = Thread.currentThread().getContextClassLoader.getResource("test.jpg").getFile
    println(path)

    val client = new HttpClient
    val formData = Map[String, Any](
      "api_key" -> "uqngbdsbwX9CsbqPeObfwzzlaUJpPDJC",
      "api_secret" -> "Q5kEO5lhl32wvb3mMhY0AIu90nEAob1o",
      "image_file" -> new File(path),
      //      "return_landmark" -> 1,
      "return_attributes" -> "gender,age",
    )
    val res = client.httpForm("https://api-cn.faceplusplus.com/facepp/v3/detect", formData)
    println(res.getBody)
  }

  def compare(): Unit = {
    val path1 = Thread.currentThread().getContextClassLoader.getResource("test.jpg").getFile
    val path2 = Thread.currentThread().getContextClassLoader.getResource("test4.jpg").getFile

    val client = new HttpClient
    val formData = Map[String, Any](
      "api_key" -> "uqngbdsbwX9CsbqPeObfwzzlaUJpPDJC",
      "api_secret" -> "Q5kEO5lhl32wvb3mMhY0AIu90nEAob1o",
      "image_file1" -> new File(path1),
      "image_file2" -> new File(path2),
    )
    val res = client.httpForm("https://api-cn.faceplusplus.com/facepp/v3/compare", formData)
    println(res.getBody)
  }

  def main(args: Array[String]): Unit = {
    compare()
  }

}
