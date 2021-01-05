package data

import java.io.File

import data.internal.{HoleDataBuilder, StampInfoBuilder}
import util.{Logger, Utils}

import scala.xml.{Node, XML}

object HoleDataParser {
  val ID_TAG = "MWDholeId"
  val VEER_ID_TAG = "PlanIdRef"
  val START_LOG_TIME_TAG = "StartLogTime"
  val END_LOG_TIME_TAG = "EndLogTime"
  val DATA_TAG = "CompactMWDdata"
  val SAMPLE_TAG = "Sample"
  val PARAMETERS_SECTION_TAG = "MWDparams"
  val PARAMETER_TAG = "Parameter"
  val SAMPLE_DATE_TAG = "TiStamp"
  val SAMPLE_VALUES_TAG = "val"
}

class HoleDataParser(private val file: File) {
  val doc = XML.loadFile(file)
  val holeDataBuilder = new HoleDataBuilder

  def parse(): Option[HoleData] = {
    parseInternal()
    try{
      val holeData = holeDataBuilder.create()
      Option.apply(holeData)
    }
    catch {
      case _: Throwable => Option.empty
    }
  }

  private def parseInternal(): Unit = {
    for(item <- doc) {
//      println(item.attributes)
      for(child <- item.child) {
        child.label match {
          case HoleDataParser.ID_TAG => parseId(child)
          case HoleDataParser.START_LOG_TIME_TAG => parseStartTime(child)
          case HoleDataParser.END_LOG_TIME_TAG => parseEndTime(child)
          case HoleDataParser.DATA_TAG => parseData(child)
          case HoleDataParser.VEER_ID_TAG => parseVeerId(child)
          case _ => Nil
        }
      }
    }
  }

  private def parseVeerId(node: Node): Unit = {
    holeDataBuilder.setVeerId(node.text)
  }

  private def parseId(node: Node): Unit = {
    holeDataBuilder.setId(node.text.toInt)
  }

  private def parseStartTime(node: Node): Unit = {
    Utils.convertData(node.text) match {
      case Some(value) => holeDataBuilder.setStartTime(value)
      case None => Logger.log("Can't parse date: " + node.text)
    }
  }

  private def parseEndTime(node: Node): Unit = {
    Utils.convertData(node.text) match {
      case Some(value) => holeDataBuilder.setEndTime(value)
      case None => Logger.log("Can's parse date: " + node.text)
    }
  }

  private def parseData(node: Node): Unit = {
    for(child <- node.child) {
      child.label match {
        case HoleDataParser.SAMPLE_TAG => parseSample(child)
        case HoleDataParser.PARAMETERS_SECTION_TAG => parseParameters(child)
        case _ => Nil
      }
    }
  }

  private def parseParameters(node: Node): Unit = {
    for(child <- node.child) {
      if(child.label == HoleDataParser.PARAMETER_TAG) {
        holeDataBuilder.addParameter(child.text)
      }
    }
  }

  private def parseSample(node: Node): Unit = {
    val builder = new StampInfoBuilder
    for(child <- node.child) {
      child.label match {
        case HoleDataParser.SAMPLE_DATE_TAG => Utils.convertData(child.text) match {
          case Some(value) => builder.setTime(value)
          case None => Logger.log("Can't parse date: " + child.text)
        }
        case HoleDataParser.SAMPLE_VALUES_TAG => {
          val values = child.text.split(" ").map(string => string.toDouble)
          builder.setDepth(values(0))
          builder.setPenetrRate(values(1))

          val dataAlign = 2
          for(i <- Range.apply(dataAlign, values.length)) {
            builder.setAdditionParameter(holeDataBuilder.parameters(i - dataAlign), values(i))
          }
        }
        case _ => Nil
      }
    }
    holeDataBuilder.addStamp(builder.create())
  }
}
