package data.internal

import java.nio.file.Path

import data.HoleData
import javafx.scene.Node
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.chart.{NumberAxis, ScatterChart, XYChart}

object ChartProvider {

  val style =
    """
       .default-color0.chart-symbol {
  -fx-background-radius: 30px;
}"""

  def getChart(holeData: HoleData): Node = {
    val chart = new ScatterChart(NumberAxis("X", 0, 25, 1), NumberAxis("Y", 0, 6, 1)) {
      title = "Penetration rate chart"
      legendSide = Side.Right
      data = ObservableBuffer(
//        xySeries("Series 1", Seq((0.1, 0.2), (1.1, 0.8), (1.9, 2.5), (3.2, 3.3), (3.9, 3.5), (5.1, 5.4))),
//        xySeries("Series 2", Seq((0, 4), (1, 1), (2, 4.5), (3, 3.5), (4, 4.25), (5, 4.5))),
//        xySeries("Series 3", Seq((0, 1), (1, 2.55), (2, 4), (3, 3), (4, 4.5), (5, 5.5))))
      xySeries("Depth", holeData.stamps.map(stampInfo => (stampInfo.depth, stampInfo.penetrRate)).toSeq))
    }
    chart.getStylesheets.add(getClass.getResource("ChartPointStyle.css").toExternalForm)
    chart
  }

  def getResource(): String = {
    val path = Path.of(getClass.getResource(".").toURI)
    path.toFile.list().
  }

  /** Create XYChart.Series from a sequence of number pairs. */
  private def xySeries(name: String, data: Seq[(Double, Double)]) =
    XYChart.Series[Number, Number](
      name,
      ObservableBuffer(data.map { case (x, y) => XYChart.Data[Number, Number](x, y) })
    )
}
