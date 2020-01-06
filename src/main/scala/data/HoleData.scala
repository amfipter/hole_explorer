package data

import java.util.Date

class HoleData(
                val id: Int,
                val startTime: Date,
                val endTime: Date,
                val parameters: Seq[String],
                val stamps: Seq[StampInfo]) {

}
