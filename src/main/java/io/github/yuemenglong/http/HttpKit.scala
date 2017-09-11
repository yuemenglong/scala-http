package io.github.yuemenglong.http

import java.net.URLEncoder
import java.net.URLDecoder

/**
  * Created by <yuemenglong@126.com> on 2017/9/11.
  */
object HttpKit {
  /**
    * Decodes the passed UTF-8 String using an algorithm that's compatible with
    * JavaScript's <code>decodeURIComponent</code> function. Returns
    * <code>null</code> if the String is <code>null</code>.
    *
    * @param s The UTF-8 encoded String to be decoded
    * @return the decoded String
    */
  def decodeURIComponent(s: String): String = {
    if (s == null) return null
    URLDecoder.decode(s, "UTF-8")
  }

  /**
    * Encodes the passed String as UTF-8 using an algorithm that's compatible
    * with JavaScript's <code>encodeURIComponent</code> function. Returns
    * <code>null</code> if the String is <code>null</code>.
    *
    * @param s The String to be encoded
    * @return the encoded String
    */
  def encodeURIComponent(s: String): String = {
    URLEncoder.encode(s, "UTF-8")
      .replaceAll("\\+", "%20")
      .replaceAll("\\%21", "!")
      .replaceAll("\\%27", "'")
      .replaceAll("\\%28", "(")
      .replaceAll("\\%29", ")")
      .replaceAll("\\%7E", "~")
  }

  def encodeObject(obj: Object): String = {
    obj.getClass.getDeclaredFields.map(field => {
      field.setAccessible(true)
      val name = field.getName
      val value = field.get(obj)
      if (value == null) {
        null
      } else {
        s"$name=${encodeURIComponent(value.toString)}"
      }
    }).filter(_ != null).mkString("&")
  }
}
