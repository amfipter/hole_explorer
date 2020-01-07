package data.internal

import java.util.Date

import data.{HoleData, StampInfo}

class HoleDataBuilder {
  var id: Int = null
  var startTime: Date = null
  var endTime: Date = null
  val parameters = new collection.mutable.ArrayBuffer[String]()
  val stamps = new collection.mutable.ArrayBuffer[StampInfo]()

  def create(): HoleData = {
    new HoleData(id, startTime, endTime, parameters.toSeq, stamps.toSeq)
  }

  def setId(id: Int): Unit = {
    this.id = id
  }

  def setStartTime(time: Date) = {
    this.startTime = time
  }

  def setEndTime(time: Date): Unit = {
    this.endTime = time
  }

  def addParameter(parameter: String): Unit = {
    parameters += parameter
  }

  def addStamp(stampInfo: StampInfo): Unit = {
    stamps += stampInfo
  }
}
