import java.io.File

import `export`.ChartExporter
import data.{HoleData, HoleDataParser, HolesData, UiSettings}
import data.internal.ChartProvider
import scalafx.geometry.{Insets, Side}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.{Alert, Button, CheckBox, ComboBox, Label}
import scalafx.scene.paint.Color._
import scalafx.scene.layout.{Border, GridPane, HBox, Priority, VBox}
import scalafx.stage.{FileChooser, Stage}
import scalafx.Includes._
import javafx.beans.value.ObservableValue
import localizer.Localizer
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.{NumberAxis, ScatterChart, XYChart}
import scalafx.stage.FileChooser.ExtensionFilter

import scala.xml.XML

object MainUI {
  val width = 1200
  val height = 900

  private val EXPORT__EXPORT_CHARTS = Localizer.getTranslation("Export charts")
  private val EXPORT__ERROR_MESSAGE = Localizer.getTranslation("Nothing to export")
  private val IMPORT__DIALOG_NAME = Localizer.getTranslation("Select xml")
  private val IMPORT__TYPE_DESCRIPTION = Localizer.getTranslation("xml description file")
  private val SELECT_FILE = Localizer.getTranslation("Select file")
  private val EXPORT = Localizer.getTranslation("Export")
  private val HOLE_ID_SELECTOR = Localizer.getTranslation("Hole ID: ")
  private val CHART_SELECTOR__SEPARATE_VIEW = Localizer.getTranslation("Separated charts")
  private val CHART_SELECTOR__INTEGRATED_VIEW = Localizer.getTranslation("Integrated chart")
}


class MainUI(private val stage: Stage) extends Scene {
  var charts: Option[Node] = Option.empty
  private var currentWidth = MainUI.width
  private var currentHeight = MainUI.height
  private val holesDataModel = new HolesData
  private val settings = new UiSettings

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
    title = MainUI.IMPORT__DIALOG_NAME
    extensionFilters.add(
      new ExtensionFilter(MainUI.IMPORT__TYPE_DESCRIPTION, Seq("*.xml"))
    )
  }

  lazy val fileCreator = new FileChooser {
    title = MainUI.EXPORT__EXPORT_CHARTS
    extensionFilters.add(
      new ExtensionFilter("png", Seq("*.png"))
    )
  }

  val selectFileButton = new Button {
    text = MainUI.SELECT_FILE
    onAction = handle {
      resetControlsData()
      val selectedFiles = Option.apply(fileChooser.showOpenMultipleDialog(stage))
      selectedFiles match {
        case Some(value) => {
          value.filter(file => file != null).foreach(file => holesDataModel.addXmlFile(file))
          fullUpdate()
        }
        case None => Nil
      }
    }
  }

  //TODO
  val exportButton = new Button {
    text = MainUI.EXPORT
    onAction = handle {
      holesDataModel.getLastHoleData() match {
        case Some(value) => {
          val file = fileCreator.showSaveDialog(stage)
          val exporter = new ChartExporter(holesDataModel, settings)
          exporter.`export`(file)
        }
        case None => {
          val alert = new Alert(Alert.AlertType.Error, MainUI.EXPORT__ERROR_MESSAGE)
          alert.showAndWait()
        }
      }
    }
  }

  //TODO remove
  val skipFirstValueCheckbox = new CheckBox {
    text = "Skip\nFirst\nValue"
    selected = settings.skipFirstValue
    onAction = handle {
      settings.skipFirstValue = !settings.skipFirstValue
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
      updateControlLabels()
    }
  }

  val chooseHoleIdLabel = new Label(MainUI.HOLE_ID_SELECTOR)

  val chooseHoleIdVBox = new VBox() {
    padding = Insets(5, 5, 0, 0)

    children = Seq(
      chooseHoleIdLabel,
      chooseHoleId
    )
  }

  val controlBox = new VBox() {
//    padding = Insets(5)
    spacing = 5
    prefWidth = 120
    children = Seq(
      selectFileButton,
      exportButton,
//      skipFirstValueCheckbox,
      chooseHoleIdVBox
//      infoControl
    )
  }

  val separatedChartsSelector = new Button {
    text = MainUI.CHART_SELECTOR__SEPARATE_VIEW

    onAction = handle {
      if(settings.selectedChartType != UiSettings.SelectedChartType.SEPARATE) {
        settings.selectedChartType = UiSettings.SelectedChartType.SEPARATE
        redrawCharts()
        chooseHoleIdVBox.setDisable(false)
        updateControlLabels()
      }
    }
  }

  val integratedChartsSelector = new Button {
    text = MainUI.CHART_SELECTOR__INTEGRATED_VIEW

    onAction = handle {
      if(settings.selectedChartType != UiSettings.SelectedChartType.INTEGRATE) {
        settings.selectedChartType = UiSettings.SelectedChartType.INTEGRATE
        redrawCharts()
        chooseHoleIdVBox.setDisable(true)
        updateControlLabels()
      }
    }
  }

  val chartSelectorSection = new HBox {
    padding = Insets(0, 0, 0, 50)
    spacing = 10
    children = Seq(
      separatedChartsSelector,
      integratedChartsSelector
    )
  }

  val chartAreaNode = new VBox {
    children = Seq(
      chartSelectorSection
    )
  }

  val mainAreaNode = new HBox {
    padding = Insets(10)
    fillHeight = true
    children = Seq(
      controlBox,
      chartAreaNode
    )
  }
  HBox.setHgrow(mainAreaNode, Priority.Always)
//  hbox.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
//    + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
//    + "-fx-border-radius: 5;" + "-fx-border-color: blue;");

  fill = White
  content = mainAreaNode

  private def redrawCharts(): Unit = {
    //TODO
    val chartNode = holesDataModel.getLastHoleData() match {
      case Some(lastHoleData) => {
        val chartProvider = new ChartProvider(holesDataModel, Option.apply(calculateChartWidth()), Option.apply(calculateChartHeight()))
        new VBox() {
          padding = Insets(5)
          children = chartProvider.getChart(settings.selectedChartType)
        }
      }
        case None => {
          new VBox {
            padding = Insets(5)
            children = Seq(
              new Label("")
            )
          }
        }
    }

    HBox.setHgrow(chartNode, Priority.Always)
    charts match {
      case Some(node) => {
        chartAreaNode.children.remove(node)
        chartAreaNode.children.add(chartNode)
        charts = Option.apply(chartNode)
      }
      case _ => {
        chartAreaNode.children.add(chartNode)
        charts = Option.apply(chartNode)
      }
    }
  }

  //TODO names for integrated charts
  private def updateControlLabels() = {
    settings.selectedChartType match {
      case UiSettings.SelectedChartType.SEPARATE => {
        holesDataModel.getLastHoleData() match {
          case Some(lastHoleData) => Main.stage.setTitle(Main.DEFAULT_NAME + ": " + lastHoleData.veerId)
          case None => Main.stage.setTitle(Main.DEFAULT_NAME)
        }
      }
      case UiSettings.SelectedChartType.INTEGRATE => {
        val veerNames = holesDataModel.getHolesIds()
          .map(id => holesDataModel.getHole(id))
          .filter(holeDataOp => holeDataOp.isDefined)
          .map(holeDataOp => holeDataOp.get)
          .map(holeData => holeData.veerId)
          .toSet
        Main.stage.setTitle(Main.DEFAULT_NAME + ": [" + veerNames.addString(new StringBuilder, ", ") + "]")
      }
      case _ => Main.stage.setTitle(Main.DEFAULT_NAME)
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
    updateControlLabels()
  }

  private def calculateChartWidth(): Int = {
    currentWidth - controlBox.width.toInt - 20
  }

  private def calculateChartHeight(): Int = {
    currentHeight - chartSelectorSection.height.toInt - 20
  }
}
