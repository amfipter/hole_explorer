package data

import java.util.Date
import scala.collection.mutable.Map
import scala.collection.Set

class StampInfo(
                 val time: Date,
                 val depth: Double,
                 val penetrRate: Double,
                 val additionParameters: Map[String, Double]) {
  assert(time != null)
  assert(depth != null && depth != Double.NaN)
  assert(penetrRate != null && penetrRate != Double.NaN)
  assert(additionParameters != null)

  def getAdditionParameters(): Set[String]={
    additionParameters.keySet
  }

  def getParameter(identifier: String): Double = {
    additionParameters.getOrElse(identifier, Double.NaN)
  }
}
