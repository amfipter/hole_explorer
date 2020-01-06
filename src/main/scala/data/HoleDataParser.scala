package data

import java.io.File

import scala.xml.XML

class HoleDataParser(private val file: File) {
  val doc = XML.loadFile(file)

  for(item <- doc) {
    println(item.attributes)
    for(child <- item.child) {
      println("LABEL: " + child.label)
      println(child)
      println("==")
    }
  }
}
