package io.github.yuemenglong.http

import java.io.InputStream


/**
  * Created by <yuemenglong@126.com> on 2017/8/31.
  */
object Kit {
  def streamToBuffer(stream: InputStream): Array[Byte] = {
    Stream.continually({
      val buffer = new Array[Byte](4096)
      val len = stream.read(buffer)
      (buffer, len)
    }).takeWhile(_._2 >= 0).flatMap { case (buffer, len) =>
      buffer.slice(0, len)
    }.toArray
  }
}
