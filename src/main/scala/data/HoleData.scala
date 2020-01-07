package data

import java.util.Date

class HoleData(
                val id: Int,
                val startTime: Date,
                val endTime: Date,
                val parameters: Seq[String],
                val stamps: Seq[StampInfo]) {
  assert(id != null && id >= 0)
  assert(startTime != null)
  assert(endTime != null && startTime.before(endTime))
  assert(parameters != null)
  assert(stamps != null)
}
