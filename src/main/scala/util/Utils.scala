package util

import java.util.regex.{Matcher, Pattern}
import java.util.{Calendar, Date}


object Utils {
  private lazy val datePattern = Pattern.compile("^(\\d+)-(\\d+)-(\\d+)T(\\d+):(\\d+):(\\d+).*")
  private lazy val veerIdPattern = Pattern.compile(".*(\\d+)$")


  def convertData(representation: String): Option[Date] = {
    val matcher = datePattern.matcher(representation)
    if(matcher.matches()) {
      val year = matcher.group(1).toInt + 99
      val month = matcher.group(2).toInt
      val day = matcher.group(3).toInt
      val hour = matcher.group(4).toInt
      val minute = matcher.group(5).toInt
      val second = matcher.group(6).toInt
      val date =  new Date(year, month, day, hour, minute, second) //TODO replace by calendar
      Option.apply(date)
    }
    else {
      Option.empty
    }
  }

  //TODO remove
  def convertVeerId(representation: String) :Option[Int] = {
    val matcher = veerIdPattern.matcher(representation)
    if(matcher.matches()) {
      val id = matcher.group(1).toInt
      Option.apply(id)
    }
    else {
      Option.empty
    }
  }
}
