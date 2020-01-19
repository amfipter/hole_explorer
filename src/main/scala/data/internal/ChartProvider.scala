package data.internal

import java.nio.file.Path

import data.HoleData
import scalafx.scene.{Cursor, Node}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.chart.{LineChart, NumberAxis, ScatterChart, XYChart}
import scalafx.scene.control.Tooltip

object ChartProvider {
  private val defaultWidth: Int = 1000

  def getChart(holeData: HoleData, isAdditional: Boolean, prefWidth: Option[Int], skipFirstValue: Boolean): Node = {
    val scales = computeScales(holeData, isAdditional)
    val chart = new LineChart(NumberAxis("Depth", scales._1, scales._2, scales._4), NumberAxis("Value", scales._1, scales._3, scales._5)) {
      title = getChartName(isAdditional)
      legendSide = Side.Right
      if(!isAdditional) {
        data = ObservableBuffer(
          xySeries("Depth", holeData.stamps.map(stampInfo => (stampInfo.depth, stampInfo.penetrRate)).toSeq, skipFirstValue))
      }
      else {
        val parameters = holeData.stamps(0).additionParameters.keySet
        val series = parameters.map(parameter => xySeries(parameter, holeData.stamps.map(stampInfo => (stampInfo.depth, stampInfo.additionParameters.get(parameter).get)), skipFirstValue))
        data = ObservableBuffer(
          series.toSeq
        )
      }
      style = "-fx-background-radius: 0px;"
    }
    chart.getData.forEach(data => data.getData.forEach(innerData => {
      val node = innerData.getNode
      Tooltip.install(node, new Tooltip("Depth: " + innerData.getXValue + "\n" + "Value: " + innerData.getYValue))
    }))
    chart.setPrefWidth(prefWidth.getOrElse(defaultWidth).toDouble)
    chart.setCursor(Cursor.CROSSHAIR);
//    chart.setHorizontalGridLinesVisible(!isAdditional)
    chart
  }

  private def computeScales(holeData: HoleData, isAdditional: Boolean): (Int, Int, Int, Double, Double) = {
    if(isAdditional) {
      val start = 0
      val end = holeData.stamps.map(stampInfo => stampInfo.depth).max.toInt + 1
      val maxValue = holeData.stamps.map(stampInfo => stampInfo.additionParameters.values.max).max.toInt + 1
      val step_ = (end.toDouble / holeData.stamps.size)
      val step = step_ - (step_ % 0.1)
      val stepx_ = maxValue.toDouble / 80
      val stepx = stepx_ - (stepx_ %0.1)
      println(step)
      (start, end, maxValue, stepx, maxValue.toDouble / 10)
    }
    else {
      val start = 0
      val end = holeData.stamps.map(stampInfo => stampInfo.depth).max.toInt + 1
      val maxValue = holeData.stamps.map(stampInfo => stampInfo.penetrRate).max.toInt + 1
      val step_ = (end.toDouble / holeData.stamps.size)
      val step = step_ - (step_ % 0.1)
      (start, end, maxValue, step * 4, maxValue.toDouble / 10)
    }
  }

  private def getChartName(isAdditional: Boolean): String = {
    isAdditional match {
      case true => "Additional options"
      case _ => "Penetration rate chart"
    }
  }

  /** Create XYChart.Series from a sequence of number pairs. */
  private def xySeries(name: String, data: Seq[(Double, Double)], skipFirstValue: Boolean) =
    XYChart.Series[Number, Number](
      name,
      ObservableBuffer((skipFirstValue match {
        case true => data.tail
        case false => data
      }).map { case (x, y) => XYChart.Data[Number, Number](x, y) })
    )
}