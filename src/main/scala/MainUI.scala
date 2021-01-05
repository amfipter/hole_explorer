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
  private val EXPORT__EXPORT_CHARTS = Localizer.getTranslation("Export charts")
  private val EXPORT__ERROR_MESSAGE = Localizer.getTranslation("Nothing to export")
  private val IMPORT__DIALOG_NAME = Localizer.getTranslation("Select xml")
  private val IMPORT__TYPE_DESCRIPTION = Localizer.getTranslation("xml description file")
  private val SELECT_FILE = Localizer.getTranslation("Select file")
  private val EXPORT = Localizer.getTranslation("Export")
  private val HOLE_ID_SELECTOR = Localizer.getTranslation("Hole ID: ")
  private val CHART_SELECTOR__SEPARATE_VIEW = Localizer.getTranslation("Separated charts")
  private val CHART_SELECTOR__INTEGRATED_VIEW = Localizer.getTranslation("Integrated chart")
  private val TITLE__INCONSISTENT_DATA = Localizer.getTranslation("ERROR: Inconsistent data")

  private val CHECKBOX_TEXT = "Skip\nFirst\nValue"
  private val UPDATE_CONTROL_SEPARATOR = ": "

  private val PNG_EXTENSION = "png"
  private val PNG_EXTENSION_FILTER = "*.png"
  private val XML_EXTENSION_FILTER = "*.xml"
}


class MainUI(private val stage: Stage) extends Scene {
  var charts: Option[Node] = Option.empty
  private var currentWidth = Main.DEFAULT_WIDTH
  private var currentHeight = Main.DEFAULT_HEIGHT
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
      new ExtensionFilter(MainUI.IMPORT__TYPE_DESCRIPTION, Seq(MainUI.XML_EXTENSION_FILTER))
    )
  }

  lazy val fileCreator = new FileChooser {
    title = MainUI.EXPORT__EXPORT_CHARTS
    extensionFilters.add(
      new ExtensionFilter(MainUI.PNG_EXTENSION, Seq(MainUI.PNG_EXTENSION_FILTER))
    )
  }

  val selectFileButton = new Button {
    text = MainUI.SELECT_FILE
    onAction = handle {
      val selectedFiles = Option.apply(fileChooser.showOpenMultipleDialog(stage))
      selectedFiles match {
        case Some(value) => {
          holesDataModel.clear()
          value.filter(file => file != null).foreach(file => holesDataModel.addXmlFile(file))
          settings.selectedHoleId = Option.empty
          fullUpdate()
        }
        case None => {
          Nil
        }
      }
    }
  }

  val exportButton = new Button {
    text = MainUI.EXPORT
    onAction = handle {
      settings.selectedHoleId match {
        case Some(holeId) => {
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
    text = MainUI.CHECKBOX_TEXT
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
      settings.selectedHoleId = Option.apply(id)
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
    spacing = 5
    prefWidth = 120
    children = Seq(
      selectFileButton,
      exportButton,
      chooseHoleIdVBox
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
  fill = White
  content = mainAreaNode

  private def redrawCharts(): Unit = {
    val chartNode = {
      val chartProvider = new ChartProvider(settings, holesDataModel, Option.apply(calculateChartWidth()), Option.apply(calculateChartHeight()))
      new VBox() {
        padding = Insets(5)
        children = chartProvider.getChart()
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

  private def updateControlLabels() = {
    settings.selectedChartType match {
      case UiSettings.SelectedChartType.SEPARATE => {
        settings.selectedHoleId match {
          case Some(holeId) => Main.stage.setTitle(Main.DEFAULT_NAME + MainUI.UPDATE_CONTROL_SEPARATOR + { holesDataModel.getHole(holeId) match {
            case Some(hole) => hole.veerId
            case None => MainUI.TITLE__INCONSISTENT_DATA
          }})
          case None => Main.stage.setTitle(Main.DEFAULT_NAME)
        }
      }
      case UiSettings.SelectedChartType.INTEGRATE => {
        Main.stage.setTitle(Main.DEFAULT_NAME + ": " + holesDataModel.collectVeerNames())
      }
      case _ => Main.stage.setTitle(Main.DEFAULT_NAME)
    }

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
