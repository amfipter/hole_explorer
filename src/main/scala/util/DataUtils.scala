package util

import java.util.{Calendar, Date}


object DataUtils {
  def convertData(representation: String): Date = {
    val date = new Date()
    println("Data to convert: " + representation)
    val calendar = Calendar.getInstance()
    //TODO parse in calendar
    null
  }
}
