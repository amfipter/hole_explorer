package data

import java.io.File

import util.Logger

import scala.collection.mutable

class HolesData {
  private val holesDataMap: mutable.HashMap[Int, HoleData] = new mutable.HashMap[Int, HoleData]()

  def addXmlFile(file: File) = {
    assert(file != null)
    val holeParser = new HoleDataParser(file)
    val holeOp = holeParser.parse()
    holeOp match {
      case Some(hole) => holesDataMap.put(hole.id, hole)
      case None => Logger.log("Can't parse file: " + file.getName)
    }
  }

  def getHolesIds(): Seq[Int] =
    holesDataMap.keySet.toSeq

  def getHole(id: Int): Option[HoleData] = {
    val hole = holesDataMap.get(id)
    hole
  }

  def clear(): Unit = {
    holesDataMap.clear()
  }

  def collectVeerNames(): String = {
    val veerNames = holesDataMap.values.filter(value => value != null).map(hole => hole.veerId).toSet
    val stringBuilder = veerNames.addString(new mutable.StringBuilder("["), ",")
    stringBuilder.addOne(']')
    stringBuilder.toString()
  }
}
