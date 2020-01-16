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

  def getChart(holeData: HoleData, isAdditional: Boolean, prefWidth: Option[Int]): Node = {
    val scales = computeScales(holeData, isAdditional)
    val chart = new LineChart(NumberAxis("Depth", scales._1, scales._2, scales._4), NumberAxis("Value", scales._1, scales._3, scales._4)) {
      title = getChartName(isAdditional)
      legendSide = Side.Right
      if(!isAdditional) {
        data = ObservableBuffer(
          xySeries("Depth", holeData.stamps.map(stampInfo => (stampInfo.depth, stampInfo.penetrRate)).toSeq))
      }
      else {
        val parameters = holeData.stamps(0).additionParameters.keySet
        val series = parameters.map(parameter => xySeries(parameter, holeData.stamps.map(stampInfo => (stampInfo.depth, stampInfo.additionParameters.get(parameter).get))))
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
    println(prefWidth)
    chart.setCursor(Cursor.CROSSHAIR);
    chart
  }

  private def computeScales(holeData: HoleData, isAdditional: Boolean): (Int, Int, Int, Double) = {
    if(isAdditional) {
      val start = 0
      val end = holeData.stamps.map(stampInfo => stampInfo.depth).max.toInt + 1
      val maxValue = holeData.stamps.map(stampInfo => stampInfo.additionParameters.values.max).max.toInt + 1
      val step = end.toDouble / holeData.stamps.size
      (start, end, maxValue, step / 2)
    }
    else {
      val start = 0
      val end = holeData.stamps.map(stampInfo => stampInfo.depth).max.toInt + 1
      val maxValue = holeData.stamps.map(stampInfo => stampInfo.penetrRate).max.toInt + 1
      val step = end.toDouble / holeData.stamps.size
      (start, end, maxValue, step / 2)
    }
  }

  private def getChartName(isAdditional: Boolean): String = {
    isAdditional match {
      case true => "Additional options"
      case _ => "Penetration rate chart"
    }
  }

  /** Create XYChart.Series from a sequence of number pairs. */
  private def xySeries(name: String, data: Seq[(Double, Double)]) =
    XYChart.Series[Number, Number](
      name,
      ObservableBuffer(data.map { case (x, y) => XYChart.Data[Number, Number](x, y) })
    )
}