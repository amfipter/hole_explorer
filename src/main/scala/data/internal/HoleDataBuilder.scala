package data.internal

import java.util.Date

import data.{HoleData, StampInfo}

object HoleDataBuilder {
  val EXCLUDED_PARAMETERS = collection.immutable.Set.newBuilder
    .addOne("TimeTag")
    .addOne("DepthTag")
    .addOne("PenetrRate")
    .result()
}

class HoleDataBuilder {
  val parameters = new collection.mutable.ArrayBuffer[String]()

  private var id: Int = Int.MinValue
  private var veerId: Int = Int.MinValue
  private var startTime: Option[Date] = Option.empty
  private var endTime: Option[Date] = Option.empty
  private val stamps = new collection.mutable.ArrayBuffer[StampInfo]()

  def create(): HoleData = {
    new HoleData(id, veerId, startTime, endTime, parameters.toSeq, stamps.toSeq)
  }

  def setId(id: Int): Unit = {
    this.id = id
  }

  def setVeerId(id: Int): Unit = {
    this.veerId = id
  }

  def setStartTime(time: Date) = {
    this.startTime = Option.apply(time)
  }

  def setEndTime(time: Date): Unit = {
    this.endTime = Option.apply(time)
  }

  def addParameter(parameter: String): Unit = {
    if(!HoleDataBuilder.EXCLUDED_PARAMETERS.contains(parameter)) {
      parameters += parameter
    }
  }

  def addStamp(stampInfo: StampInfo): Unit = {
    stamps += stampInfo
  }
}
