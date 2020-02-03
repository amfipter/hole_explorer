package `export`

import java.io.File

import data.{HoleData, HolesData, UiSettings}
import data.internal.ChartProvider
import javax.imageio.ImageIO
import localizer.Localizer
import scalafx.embed.swing.SwingFXUtils
import scalafx.geometry.Insets
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.Label
import scalafx.scene.image.WritableImage
import scalafx.scene.layout.{HBox, Pane, Priority, StackPane, VBox}
import scalafx.stage.{Screen, Stage}
import util.Logger

object ChartExporter {
  lazy private val SCREEN_SIZE = Screen.primary.bounds

  private val VEER_ID = Localizer.getTranslation("Veer ID: ")
  private val HOLE_ID = Localizer.getTranslation("Hole ID: ")
  private val MESSAGE__INCONSISTENT_DATA = Localizer.getTranslation("ERROR: Inconsistent data")
}

class ChartExporter(private val holesData: HolesData, private val settings: UiSettings) {
  val pane = new StackPane()
  lazy val stage = new Stage()
  lazy val scene = new Scene(pane, ChartExporter.SCREEN_SIZE.width, ChartExporter.SCREEN_SIZE.height * 0.8)

  stage.scene = scene
//  stage.show()
  val node = redraw()

  def export(file_ :File): Unit = {
    val image = new WritableImage(ChartExporter.SCREEN_SIZE.width.toInt, (ChartExporter.SCREEN_SIZE.height * 0.8).toInt)
    Thread.sleep(1000)
    node.snapshot(null, image)
    //TODO handle exceptions
    val file = if(!file_.getName.endsWith(".png")) {
      new File(file_.getName + ".png") //TODO
    } else {
      file_
    }
    if(file.exists()) {
      file.delete()
    }
    file.createNewFile()
    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file)
    println(ChartExporter.SCREEN_SIZE)
  }

  def redraw(): Pane = {
    scene.getChildren.clear()

    val info = new HBox() {
      children = settings.selectedChartType match {
        case UiSettings.SelectedChartType.SEPARATE => {
          Seq(
            new Label(ChartExporter.VEER_ID + getVeerId() + "\t\t"),
            new Label(ChartExporter.HOLE_ID + {settings.selectedHoleId match {
              case Some(id) => id
              case None => ChartExporter.MESSAGE__INCONSISTENT_DATA
            }}))
        }
        case UiSettings.SelectedChartType.INTEGRATE => {
          Seq(
            new Label(ChartExporter.VEER_ID + holesData.collectVeerNames())
          )
        }
        case _ => {
          Logger.logWithTrace("Unsupported chart type for export operation")
          Seq()
        }
      }
    }

    val chartProvider = new ChartProvider(settings, holesData, Option.apply(calculateChartWidth()), Option.apply(calculateChartHeight()))
    val chartsArea = new VBox() {
      children = chartProvider.getChart()
    }

    val charts = new VBox {
      padding = Insets(5)
      children = Seq(
        info,
        chartsArea
      )
    }



//    val hbox = new HBox() {
//      padding = Insets(5)
//      fillHeight = true
//      children = Seq(
//        info,
//        charts
//      )
//    }
    HBox.setHgrow(info, Priority.Always)

    scene.getChildren.add(charts)
    charts
  }

  private def getVeerId(): String = settings.selectedHoleId match {
    case Some(holeId) => {
      holesData.getHole(holeId) match {
        case Some(holeData) => holeData.veerId
        case None => ChartExporter.MESSAGE__INCONSISTENT_DATA
      }
    }
    case None => ChartExporter.MESSAGE__INCONSISTENT_DATA
  }

  private def calculateChartWidth(): Int = {
    val width = ChartExporter.SCREEN_SIZE.width - 10
    width.toInt
  }

  private def calculateChartHeight(): Int = {
    val height = ChartExporter.SCREEN_SIZE.height - 10
    height.toInt
  }
}
