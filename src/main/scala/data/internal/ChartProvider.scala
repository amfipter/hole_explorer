package data.internal

import javafx.scene.{chart => jfxsc}
import data.{HoleData, HolesData, UiSettings}
import localizer.Localizer
import scalafx.scene.{Cursor, Node}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.chart.XYChart.{Series}
import scalafx.scene.chart.{CategoryAxis, LineChart, NumberAxis, StackedBarChart, XYChart}
import scalafx.scene.control.{Label, Tooltip}
import util.{ChartUtils}

import scala.collection.mutable

object ChartProvider {
  private val DEFAULT_WIDTH: Int = 1000
  private val DEFAULT_HEIGHT: Int = 800

  private val DEPTH = Localizer.getTranslation("Depth")
  private val VALUE = Localizer.getTranslation("Value")
  private val ADDITIONAL_OPTIONS = Localizer.getTranslation("Additional options")
  private val PENETRATION_RATE_CHART = Localizer.getTranslation("Penetration rate chart")
  private val HOLE_ID = Localizer.getTranslation("Hole ID: ")

  private def getDefaultNode() = new Label("")
}

class ChartProvider(private val uiSettings: UiSettings, private val holesData: HolesData, private val prefWidth: Option[Int], private val prefHeight: Option[Int]) {
  def getChart(): Seq[Node] = {
    uiSettings.selectedChartType match {
      case UiSettings.SelectedChartType.SEPARATE => Seq(getSeparatedChart(false, true), getSeparatedChart(true, true))
      case UiSettings.SelectedChartType.INTEGRATE => Seq(getIntegratedChartProto())
      case _ => Seq(new Label("NOT IMPLEMENTED"))
    }
  }

  private def getIntegratedChartProto() :Node = {
    val xAxis = new CategoryAxis();
    xAxis.setCategories(ObservableBuffer(holesData.getHolesIds().map(id => id.toString)))
    xAxis.setLabel("x")
    val yAxis = new NumberAxis()
    yAxis.setLabel("y")
    val chart = new StackedBarChart[String, Number](xAxis, yAxis)
    val colorsMap = new mutable.HashMap[jfxsc.XYChart.Data[String, Number], String]()

    holesData.getHolesIds().filter(id => holesData.getHole(id).isDefined).foreach(id => {
      val holeData = holesData.getHole(id).get
      val coloredData = ChartUtils.colorSequence(holeData.stamps.map(stampInfo => (stampInfo.depth, stampInfo.penetrRate)))

      val series1 = new Series[String, Number]()
      coloredData.foreach(colored => {
        println((holeData.id.toString, colored))
        val data = XYChart.Data[String, Number](holeData.id.toString, colored._1._1)
        colorsMap.put(data, colored._2)
        series1.getData.add(data)
      })
      chart.getData.add(series1)
    })

    chart.getData.forEach(data => data.getData.forEach(innerData => {
      val node = innerData.getNode
      if(node != null) {
        colorsMap.get(innerData) match {
          case Some(color) => node.setStyle("-fx-bar-fill:" + color + ";")
          case None => node.setStyle("-fx-bar-fill: black;")
        }
      }
    }))

    chart.setPrefWidth(prefWidth.getOrElse(ChartProvider.DEFAULT_WIDTH).toDouble)
    chart.setPrefHeight(prefHeight.getOrElse(ChartProvider.DEFAULT_HEIGHT).toDouble)
    chart.setLegendVisible(false)
    chart
  }

  private def getIntegratedChart() :Node = {
    val scales = holesData.getHolesIds()
      .map(id => holesData.getHole(id))
      .map(hole => computeScalesSafe(hole))
      .reduce((x, y) => (Math.max(x._1, y._1), Math.max(x._2, y._2), Math.max(x._3, y._3), Math.max(x._4, y._4), Math.max(x._5, y._5)))

    val chart = new LineChart(NumberAxis(ChartProvider.DEPTH, scales._1, scales._2, scales._4), NumberAxis(ChartProvider.VALUE, scales._1, scales._3, scales._5)) {
      title = getChartName(false)
      legendSide = Side.Right
      style = "-fx-background-radius: 0px;"
      data = ObservableBuffer(holesData.getHolesIds()
        .map(id => holesData.getHole(id))
        .filter(hole1 => hole1.isDefined)
        .map(holeOpt => holeOpt.get)
        .map(hole => xySeries(hole.id.toString, hole.stamps.map(stampInfo => (stampInfo.depth, stampInfo.penetrRate)), true)))
    }
    chart.getData.forEach(data => data.getData.forEach(innerData => {
      val node = innerData.getNode
      Tooltip.install(node, new Tooltip(ChartProvider.HOLE_ID + ": " + data.getName + "\n"
        + ChartProvider.DEPTH + ": " + innerData.getXValue + "\n"
        + ChartProvider.VALUE + ": " + innerData.getYValue))
    }))

    chart.setPrefWidth(prefWidth.getOrElse(ChartProvider.DEFAULT_WIDTH).toDouble)
    chart.setPrefHeight(prefHeight.getOrElse(ChartProvider.DEFAULT_HEIGHT).toDouble)
    chart.setCursor(Cursor.CROSSHAIR);
    chart.setLegendSide(Side.Bottom)
    chart.setLegendVisible(true)
    chart
  }

  private def getSeparatedChart(isAdditional: Boolean, skipFirstValue: Boolean): Node = {
    uiSettings.selectedHoleId match {
      case Some(holeDataId) => {
        val holeDataOp = holesData.getHole(holeDataId)
        holeDataOp match {
          case Some(holeData) => {
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
              Tooltip.install(node, new Tooltip(ChartProvider.DEPTH + innerData.getXValue + "\n" + ChartProvider.VALUE + ": " + innerData.getYValue))
            }))
            chart.setPrefWidth(prefWidth.getOrElse(ChartProvider.DEFAULT_WIDTH).toDouble)
            chart.setCursor(Cursor.CROSSHAIR);
            chart.setLegendSide(Side.Bottom)
            chart.setLegendVisible(isAdditional)
            chart
          }
          case None => ChartProvider.getDefaultNode()
        }
      }
      case None => ChartProvider.getDefaultNode()
    }
  }

  private def computeScales(holeData: HoleData, isAdditional: Boolean): (Int, Int, Int, Double, Double) = {
    val end = holeData.stamps.map(stampInfo => stampInfo.depth).max.toInt + 1
    val stepx = end.toDouble / 20
    if(isAdditional) {
      val start = 0
      val maxValue = holeData.stamps.map(stampInfo => stampInfo.additionParameters.values.max).max.toInt + 1
      (start, end, maxValue, stepx, maxValue.toDouble / 10)
    }
    else {
      val start = 0
      val maxValue = holeData.stamps.map(stampInfo => stampInfo.penetrRate).max.toInt + 1
      (start, end, maxValue, stepx, maxValue.toDouble / 10)
    }
  }

  private def computeScalesSafe(holeData: Option[HoleData]): (Int, Int, Int, Double, Double) = {
    holeData match {
      case Some(value) => computeScales(value, false)
      case None => (0,0,0,0F,0F)
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