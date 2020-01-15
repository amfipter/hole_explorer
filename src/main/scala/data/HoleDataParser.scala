package data

import java.io.File

import data.internal.{HoleDataBuilder, StampInfoBuilder}
import util.DataUtils

import scala.xml.{Node, XML}

object HoleDataParser {
  val ID_TAG = "ReportId"
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

//  parseInternal()

  def parse(): HoleData = {
    parseInternal()
    holeDataBuilder.create()
  }

  private def parseInternal(): Unit = {
    for(item <- doc) {
      println(item.attributes)
      for(child <- item.child) {
        child.label match {
          case HoleDataParser.ID_TAG => parseId(child)
          case HoleDataParser.START_LOG_TIME_TAG => parseStartTime(child)
          case HoleDataParser.END_LOG_TIME_TAG => parseEndTime(child)
          case HoleDataParser.DATA_TAG => parseData(child)
          case _ => Nil
        }
      }
    }
  }

  private def parseId(node: Node): Unit = {
    holeDataBuilder.setId(node.text.toInt)
  }

  private def parseStartTime(node: Node): Unit = {
    holeDataBuilder.setStartTime(DataUtils.convertData(node.text))
  }

  private def parseEndTime(node: Node): Unit = {
    holeDataBuilder.setEndTime(DataUtils.convertData(node.text))
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
        case HoleDataParser.SAMPLE_DATE_TAG => builder.setTime(DataUtils.convertData(child.text))
        case HoleDataParser.SAMPLE_VALUES_TAG => {
          val values = child.text.split(" ").map(string => string.toDouble)
          builder.setDepth(values(0))
          builder.setPenetrRate(values(1))

          val dataAlign = 2
          for(i <- Range.apply(dataAlign, values.length)) {
            builder.setAdditionParameter(holeDataBuilder.parameters(i - dataAlign), values(i))
          }

          println(values(0))
        }
        case _ => Nil
      }
    }
    holeDataBuilder.addStamp(builder.create())
  }
}
