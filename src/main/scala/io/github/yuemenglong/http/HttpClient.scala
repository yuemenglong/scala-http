package io.github.yuemenglong.http


import java.io.File
import java.util

import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.RequestBuilder.post
import org.apache.http.client.methods.{HttpEntityEnclosingRequestBase, HttpGet, HttpPost}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.{FileBody, StringBody}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import org.apache.http.message.BasicNameValuePair

import scala.util.matching.Regex

class HttpResponse(status: Int, data: Array[Byte]) {
  def getBody: String = {
    new String(data)
  }

  def getStatus: Int = status

  def getData: Array[Byte] = data

  override def toString: String = {
    s"Status: $status, data: $getBody"
  }
}

class HttpClient {

  val cookiePattern: Regex = "^(.+)=(.*)$".r
  val colonPattern: Regex = "(.+):(.*)".r

  var cookies: Map[String, String] = Map()
  var headers: Map[String, String] = Map()

  def setCookie(key: String, value: String): Unit = {
    cookies += (key -> value)
  }

  def setHeaders(headers: Map[String, String]): Unit = {
    this.headers = headers
  }

  def setCookieString(cookieString: String): Unit = {
    cookieString.trim.split(";").map(_.trim).foreach {
      case cookiePattern(key, value) => cookies += (key.trim -> value.trim)
    }
  }

  def getCookieString: String = {
    cookies.map { case (name, value) => s"$name=$value" }.mkString(";")
  }

  def setHeader(key: String, value: String): Unit = {
    headers += (key -> value)
  }

  def setHeanderString(headerString: String): Unit = {
    headerString.trim.split("\n").foreach {
      case colonPattern("Cookie", value) => setCookieString(value)
      case colonPattern(key, value) => headers += (key.trim -> value.trim)
    }
  }

  def generateResponse(response: org.apache.http.HttpResponse): HttpResponse = {
    val status = response.getStatusLine.getStatusCode
    val stream = response.getEntity.getContent
    val buffer = Kit.streamToBuffer(stream)
    new HttpResponse(status, buffer)
  }

  def httpForm(url: String, data: Object): HttpResponse = {
    val map: Map[String, Any] = data.getClass.getDeclaredFields.map(f => {
      f.setAccessible(true)
      val name = f.getName
      val value = f.get(data)
      (name, value)
    })(collection.breakOut)
    httpForm(url, map)
  }

  def commonSetHeader(request: HttpEntityEnclosingRequestBase): Unit = {
    headers.foreach { case (name, value) =>
      request.setHeader(name, value)
    }
    if (cookies.nonEmpty) {
      request.setHeader("Cookie", getCookieString)
    }
  }

  def createClient(): CloseableHttpClient = HttpClientBuilder.create.useSystemProperties.build

  def httpGet(url: String): HttpResponse = {
    val request = new HttpGet(url)
    val response = createClient().execute(request)
    generateResponse(response)
  }

  def httpJson(url: String, data: String): HttpResponse = {
    val request = new HttpPost(url)
    commonSetHeader(request)
    val entity = new StringEntity(data)
    entity.setContentEncoding("UTF-8")
    entity.setContentType(ContentType.APPLICATION_JSON.toString)
    post.setEntity(entity)
    val response = createClient().execute(request)
    generateResponse(response)
  }

  def httpPost(url: String, data: String, contentType: ContentType): HttpResponse = {
    val request = new HttpPost(url)
    commonSetHeader(request)
    val entity = new StringEntity(data)
    entity.setContentEncoding("UTF-8")
    entity.setContentType(contentType.toString)
    post.setEntity(entity)
    val response = createClient().execute(request)
    generateResponse(response)
  }

  def httpForm(url: String, data: Map[String, Any]): HttpResponse = {
    val request = new HttpPost(url)
    commonSetHeader(request)
    val hasFile = data.toArray.exists(_._2.isInstanceOf[File])
    if (!hasFile) {
      val params = new util.ArrayList[BasicNameValuePair]()
      data.foreach { case (name, value) =>
        params.add(new BasicNameValuePair(name, value.toString))
      }
      request.setEntity(new UrlEncodedFormEntity(params))
    } else {
      val multiBuilder = MultipartEntityBuilder.create()
      data.foreach { case (name, value) =>
        value match {
          case file: File => multiBuilder.addPart(name, new FileBody(file))
          case _ => multiBuilder.addPart(name, new StringBody(value.toString, ContentType.TEXT_PLAIN))
        }
      }
      request.setEntity(multiBuilder.build())
    }
    val response = createClient().execute(request)
    generateResponse(response)
  }
}

