package util

import java.util.regex.{Matcher, Pattern}
import java.util.{Calendar, Date}


object DataUtils {
  def convertData(representation: String): Date = {
    val date = new Date()
    println("Data to convert: " + representation)
    val pattern = Pattern.compile("^(\\d+)-(\\d+)-(\\d+)T(\\d+):(\\d+):(\\d+).*")
    val matcher = pattern.matcher(representation)
    if(matcher.matches()) {
      println(matcher.group(0))
      val year = matcher.group(1).toInt + 99
      val month = matcher.group(2).toInt
      val day = matcher.group(3).toInt
      val hour = matcher.group(4).toInt
      val minute = matcher.group(5).toInt
      val second = matcher.group(6).toInt
      val date =  new Date(year, month, day, hour, minute, second) //TODO replace by calendar
      println(date)
      return date
    }
    null
  }
}
