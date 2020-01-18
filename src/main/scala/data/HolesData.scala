package data

import java.io.File

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class HolesData {
  private val holesDataMap: mutable.HashMap[Int, HoleData] = new mutable.HashMap[Int, HoleData]()
  private var lastHoleData: Option[HoleData] = Option.empty

  def addXmlFile(file: File) = {
    assert(file != null)
    val dataParser = new HoleDataParser(file)
    val data = dataParser.getHoleData()
    holesDataMap.put(data.id, data)
  }

  def getLastHoleData(): Option[HoleData] =
    lastHoleData

  def getHolesIds(): Seq[Int] =
    holesDataMap.keySet.toSeq

  def getHole(id: Int): Option[HoleData] = {
    val hole = holesDataMap.get(id)
    lastHoleData = hole
    hole
  }

  def clear(): Unit = {
    holesDataMap.clear()
    lastHoleData = Option.empty
  }

}
