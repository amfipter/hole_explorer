import java.io.File

import data.HoleDataParser
import data.internal.ChartProvider
import scalafx.geometry.{Insets, Side}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.Button
import scalafx.scene.paint.Color._
import scalafx.scene.layout.{Border, GridPane, HBox, Priority, VBox}
import scalafx.stage.{FileChooser, Stage}
import scalafx.Includes._
import javafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.{NumberAxis, ScatterChart, XYChart}
import scalafx.stage.FileChooser.ExtensionFilter

import scala.xml.XML

object MainUI {
  val width = 1200
  val height = 800
}


class MainUI(private val stage: Stage) extends Scene {
  var charts: Option[Node] = Option.empty
  private var currentWidth = MainUI.width
  private var currentHeight = MainUI.height

  this.widthProperty().addListener{
    (value: ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
      currentWidth = newVal.intValue()
      update()
  }
  this.heightProperty().addListener{
    (value: ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
      currentHeight = newVal.intValue()
      update()
  }

  lazy val fileChooser = new FileChooser {
    title = "Select xml"
    extensionFilters.add(
      new ExtensionFilter("xml description file", Seq("*.xml"))
    )
  }
  var selectedFile: File = null

  val selectFileButton = new Button {
    text = "Select file"
    onAction = handle {
      selectedFile = fileChooser.showOpenDialog(stage)
      update()
    }
  }

  val controlBox = new VBox() {
    padding = Insets(5)
    prefWidth = 100
    children = Seq(
      selectFileButton
    )
  }

  val hbox = new HBox {
    padding = Insets(10)
    fillHeight = true
    children = Seq(
      controlBox
    )
  }
  HBox.setHgrow(hbox, Priority.Always)
//  hbox.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
//    + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
//    + "-fx-border-radius: 5;" + "-fx-border-color: blue;");

  fill = White
  content = hbox

  private def update(): Unit = {
    if(selectedFile != null) {
      val dataParser = new HoleDataParser(selectedFile)
      val data = dataParser.parse()
      val vbox = new VBox() {
        padding = Insets(5)
        children = Seq(
          ChartProvider.getChart(data, false, Option.apply(calculateChartWidth())),
          ChartProvider.getChart(data, true, Option.apply(calculateChartWidth()))
        )
      }
      HBox.setHgrow(vbox, Priority.Always)
      charts match {
        case Some(node) => {
          hbox.children.remove(node)
          hbox.children.add(vbox)
          charts = Option.apply(vbox)
        }
        case _ => {
          hbox.children.add(vbox)
          charts = Option.apply(vbox)
        }
      }
    }
  }

  private def calculateChartWidth(): Int = {
    currentWidth - controlBox.width.toInt - 20
  }
}
