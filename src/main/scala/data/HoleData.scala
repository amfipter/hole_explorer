package data

import java.util.Date

//TODO update builder with optional parameters
class HoleData(
                val id: Int,
                val veerId: String,
                val startTime: Option[Date],
                val endTime: Option[Date],
                val parameters: Seq[String],
                val stamps: Seq[StampInfo]) {
  assert(id >= 0)
  assert(veerId != null)
  assert(startTime != null)
  assert(endTime != null)
  assert(parameters != null)
  assert(stamps != null)
}
