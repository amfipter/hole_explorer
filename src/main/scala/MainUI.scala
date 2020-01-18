import java.io.File

import `export`.ChartExporter
import data.{HoleData, HoleDataParser, HolesData}
import data.internal.ChartProvider
import scalafx.geometry.{Insets, Side}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.{Alert, Button, CheckBox, ComboBox, Label}
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

  private val NO_DATA_LABEL = new Label("NO DATA")
}


class MainUI(private val stage: Stage) extends Scene {
  var charts: Option[Node] = Option.empty
  private var currentWidth = MainUI.width
  private var currentHeight = MainUI.height
  private var skipFirstValue = true
  private val holesDataModel = new HolesData

  this.widthProperty().addListener{
    (value: ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
      currentWidth = newVal.intValue()
      redrawCharts()
  }
  this.heightProperty().addListener{
    (value: ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
      currentHeight = newVal.intValue()
      redrawCharts()
  }

  lazy val fileChooser = new FileChooser {
    title = "Select xml"
    extensionFilters.add(
      new ExtensionFilter("xml description file", Seq("*.xml"))
    )
  }

  lazy val fileCreator = new FileChooser {
    title = "Export charts"
    extensionFilters.add(
      new ExtensionFilter("Jpeg", Seq("*.jpg"))
    )
  }

  val selectFileButton = new Button {
    text = "Select file"
    onAction = handle {
      resetControlsData()
      val selectedFiles = fileChooser.showOpenMultipleDialog(stage)
      selectedFiles.filter(file => file != null).foreach(file => holesDataModel.addXmlFile(file))
      fullUpdate()
    }
  }

  val exportButton = new Button {
    text = "Export"
    onAction = handle {
      holesDataModel.getLastHoleData() match {
        case Some(value) => {
          val file = fileCreator.showSaveDialog(stage)
          val exporter = new ChartExporter(value)
          exporter.`export`(file)
        }
        case None => {
          val alert = new Alert(Alert.AlertType.Error, "Nothing to export")
          alert.showAndWait()
        }
      }
    }
  }

  val skipFirstValueCheckbox = new CheckBox {
    text = "Skip first value"
    selected = skipFirstValue
    onAction = handle {
      skipFirstValue = !skipFirstValue
      redrawCharts()
    }
  }

  val chooseHoleId = new ComboBox[Int]() {
    items = {
      val options = new ObservableBuffer[Int]()
      options.addAll(holesDataModel.getHolesIds())
      options
    }

    onAction = handle {
      val id = value.get()
      holesDataModel.getHole(id)
      redrawCharts()
    }
  }

  val chooseHoleIdLabel = new Label("Hole ID:")

  val chooseHoleIdVBox = new VBox {
    padding = Insets(5, 5, 0, 0)

    children = Seq(
      chooseHoleIdLabel,
      chooseHoleId
    )
  }

  val controlBox = new VBox() {
    padding = Insets(5)
    prefWidth = 100
    children = Seq(
      selectFileButton,
      exportButton,
      skipFirstValueCheckbox,
      chooseHoleIdVBox
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

  private def redrawCharts(): Unit = {
    val vbox = new VBox() {
      padding = Insets(5)
      children = Seq(
        holesDataModel.getLastHoleData() match {
          case Some(value) => ChartProvider.getChart(value, false, Option.apply(calculateChartWidth()), skipFirstValue)
          case None => new Label("")
        },
        holesDataModel.getLastHoleData() match {
          case Some(value) => ChartProvider.getChart(value, true, Option.apply(calculateChartWidth()), skipFirstValue)
          case None => new Label("")
        }
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

  private def resetControlsData(): Unit = {
    holesDataModel.clear()
  }

  private def fullUpdate(): Unit = {
    val options = new ObservableBuffer[Int]()
    options.addAll(holesDataModel.getHolesIds())
    chooseHoleId.setItems(options)
    redrawCharts()
  }

  private def calculateChartWidth(): Int = {
    currentWidth - controlBox.width.toInt - 20
  }
}
