import java.io.{File, FileOutputStream}
import java.nio.file.Paths
import java.util.Date

import scala.io.Source

/**
  * Created by <yuemenglong@126.com> on 2017/10/17.
  */
object Watcher {

  def getFileSet(dir: File, fn: (String) => Boolean): Set[String] = {
    dir.listFiles().map(_.getName).filter(fn).toSet
  }

  def watch(dir: File): Unit = {
    val sample = Thread.currentThread().getContextClassLoader.getResource("sample/sample.jpg").getFile
    val jpgSet = getFileSet(dir, name => name.endsWith(".jpg")).map(_.replace(".jpg", ""))
    val jsonSet = getFileSet(dir, name => name.endsWith(".json")).map(_.replace(".json", ""))
    val working = jpgSet -- jsonSet
    if (working.nonEmpty) {
      println("New Pic")
    }
    working.foreach(name => {
      val file = Paths.get(dir.getAbsolutePath, name + ".jpg").toString
      val detect = FacePlusPlus.detect(file)
      val score = FacePlusPlus.compare(sample, file)
      println(name, score)

      val jsonPath = Paths.get(dir.getAbsolutePath, s"${score}_$name.json").toString
      val from = Paths.get(dir.getAbsolutePath, s"$name.jpg").toString
      val to = Paths.get(dir.getAbsolutePath, s"${score}_$name.jpg").toString
      val fs = new FileOutputStream(jsonPath)
      new File(from).renameTo(new File(to))
      fs.write(detect.getBytes())
      fs.close()
    })
    println("Finish Round", new Date())
  }

  def generateHtml(files: Array[File]): Unit = {
    val arr = files.map(f => {
      val path = "file:///" + f.getAbsolutePath.replaceAll("\\\\", "/")
      val res = Source.fromFile(f.getAbsolutePath.replace(".jpg", ".json"))
        .getLines().mkString("")
      val score = f.getName.split("_")(0)
      s"""{"path":"$path","res":$res,"score":$score}"""
    }).mkString(",")
    val info = s"[$arr]"
    val tpl = Source.fromInputStream(Thread.currentThread().getContextClassLoader
      .getResourceAsStream("template.html")).getLines().mkString("\n")
    val html = tpl.replace("//IMG_INFO", info)
    val fs = new FileOutputStream("index.html")
    fs.write(html.getBytes())
    fs.close()
  }


  def main(args: Array[String]): Unit = {
    generateHtml(Array(new File("D:/pic-watch/91.705_timg (8).jpg")))
    //    val dir = new File("D:/pic-watch")
    //    while (true) {
    //      watch(dir)
    //      Thread.sleep(1000)
    //    }
  }
}
