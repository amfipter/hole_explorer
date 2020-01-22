package data.internal

import java.nio.file.Path

import data.{HoleData, UiSettings}
import localizer.Localizer
import scalafx.scene.{Cursor, Node}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.chart.{LineChart, NumberAxis, ScatterChart, XYChart}
import scalafx.scene.control.{Label, Tooltip}

object ChartProvider {
  private val DEFAULT_WIDTH: Int = 1000

  private val DEPTH = Localizer.getTranslation("Depth")
  private val VALUE = Localizer.getTranslation("Value")
  private val ADDITIONAL_OPTIONS = Localizer.getTranslation("Additional options")
  private val PENETRATION_RATE_CHART = Localizer.getTranslation("Penetration rate chart")
}

class ChartProvider(private val holeData: HoleData, private val prefWidth: Option[Int]) {


  def getChart(chartType: UiSettings.SelectedChartType.Value): Seq[Node] = {
    chartType match {
      case UiSettings.SelectedChartType.SEPARATE => Seq(getSeparatedChart(false, true), getSeparatedChart(true, true))
      case _ => Seq(new Label("NOT IMPLEMENTED"))
    }
  }

  private def getSeparatedChart(isAdditional: Boolean, skipFirstValue: Boolean): Node = {
    val scales = computeScales(holeData, isAdditional)
    val chart = new LineChart(NumberAxis(ChartProvider.DEPTH, scales._1, scales._2, scales._4), NumberAxis(ChartProvider.VALUE, scales._1, scales._3, scales._5)) {
      title = getChartName(isAdditional)
      legendSide = Side.Right
      if(!isAdditional) {
        data = ObservableBuffer(
          xySeries(ChartProvider.DEPTH, holeData.stamps.map(stampInfo => (stampInfo.depth, stampInfo.penetrRate)).toSeq, skipFirstValue))
      }
      else {
        val parameters = holeData.stamps(0).additionParameters.keySet
        val series = parameters.map(parameter => xySeries(Localizer.getTranslation(parameter), holeData.stamps.map(stampInfo => (stampInfo.depth, stampInfo.additionParameters.get(parameter).get)), skipFirstValue))
        data = ObservableBuffer(
          series.toSeq
        )
      }
      style = "-fx-background-radius: 0px;"
    }
    chart.getData.forEach(data => data.getData.forEach(innerData => {
      val node = innerData.getNode
      Tooltip.install(node, new Tooltip(ChartProvider.DEPTH + ": " + innerData.getXValue + "\n" + ChartProvider.VALUE + ": " + innerData.getYValue))
    }))
    chart.setPrefWidth(prefWidth.getOrElse(ChartProvider.DEFAULT_WIDTH).toDouble)
    chart.setCursor(Cursor.CROSSHAIR);
    chart.setLegendSide(Side.Bottom)
    chart.setLegendVisible(isAdditional)
    chart
  }

  private def computeScales(holeData: HoleData, isAdditional: Boolean): (Int, Int, Int, Double, Double) = {
    val end = holeData.stamps.map(stampInfo => stampInfo.depth).max.toInt + 1
    val stepx = end.toDouble / 20
    if(isAdditional) {
      val start = 0
      val maxValue = holeData.stamps.map(stampInfo => stampInfo.additionParameters.values.max).max.toInt + 1
      val step_ = (end.toDouble / holeData.stamps.size)
      val step = step_ - (step_ % 0.1)
      //TODO Remove
//      val stepx_ = maxValue.toDouble / 80
//      val stepx = stepx_ - (stepx_ %0.1)
      println(step)
      (start, end, maxValue, stepx, maxValue.toDouble / 10)
    }
    else {
      val start = 0
      //TODO Remove
//      val end = holeData.stamps.map(stampInfo => stampInfo.depth).max.toInt + 1
      val maxValue = holeData.stamps.map(stampInfo => stampInfo.penetrRate).max.toInt + 1
      val step_ = (end.toDouble / holeData.stamps.size)
      val step = step_ - (step_ % 0.1)
      (start, end, maxValue, stepx, maxValue.toDouble / 10)
    }
  }

  private def getChartName(isAdditional: Boolean): String = {
    isAdditional match {
      case true => ChartProvider.ADDITIONAL_OPTIONS
      case _ => ChartProvider.PENETRATION_RATE_CHART
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