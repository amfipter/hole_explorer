package data.internal

import java.util.Date

import data.StampInfo

import scala.collection.mutable

class StampInfoBuilder {
  var time: Date = null
  var depth = Double.NaN
  var penetrRate = Double.NaN
  val otherParameters = new mutable.HashMap[String, Double]()

  def create(): StampInfo = {
    new StampInfo(time, depth, penetrRate, otherParameters)
  }

  def setTime(time: Date): Unit = {
    this.time = time
  }

  def setDepth(depth: Double): Unit = {
    this.depth = depth
  }

  def setPenetrRate(rate: Double): Unit = {
    this.penetrRate = rate
  }

  def setAdditionParameter(identifier: String, value: Double): Unit = {
    otherParameters.put(identifier, value)
  }
}
