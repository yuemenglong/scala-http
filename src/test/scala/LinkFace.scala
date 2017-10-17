import java.io.{BufferedReader, File, InputStreamReader}

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.{FileBody, StringBody}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

object LinkFace {
  def HttpClientPost(): Unit = {
    val api_id = "5f04ffa3d2cb4f3081bc731c3c3cb012"
    val api_secret = "3a0a4bf579d34442bac037c733e5133e"
    val filepath1: String = Thread.currentThread().getContextClassLoader.getResource("test.jpg").getFile //图片1路径
    val filepath2: String = Thread.currentThread().getContextClassLoader.getResource("test2.jpg").getFile //图片1路径
    val POST_URL = "https://cloudapi.linkface.cn/identity/historical_selfie_verification"

    val httpclient = new DefaultHttpClient
    val post = new HttpPost(POST_URL)
    val id = new StringBody(api_id)
    val secret = new StringBody(api_secret)
    val fileBody1 = new FileBody(new File(filepath1))
    val fileBody2 = new FileBody(new File(filepath2))
    val entity = new MultipartEntity
    entity.addPart("selfie_file", fileBody1)
    entity.addPart("historical_selfie_file", fileBody2)
    entity.addPart("api_id", id)
    entity.addPart("api_secret", secret)
    post.setEntity(entity)
    val response = httpclient.execute(post)
    if (response.getStatusLine.getStatusCode == 200) {
      val entitys = response.getEntity
      val reader = new BufferedReader(new InputStreamReader(entitys.getContent))
      val line = reader.readLine
      System.out.println(line)
    }
    else {
      val r_entity = response.getEntity
      val responseString = EntityUtils.toString(r_entity)
      System.out.println("错误码是：" + response.getStatusLine.getStatusCode + "  " + response.getStatusLine.getReasonPhrase)
      System.out.println("出错原因是：" + responseString)
      //你需要根据出错的原因判断错误信息，并修改
    }
    httpclient.getConnectionManager.shutdown()
  }

  def main(args: Array[String]): Unit = {
    HttpClientPost()
  }

}
