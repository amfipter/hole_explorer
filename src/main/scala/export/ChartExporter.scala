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

object ChartExporter {
  lazy private val SCREEN_SIZE = Screen.primary.bounds

  private val VEER_ID = Localizer.getTranslation("Veer ID: ")
  private val HOLE_ID = Localizer.getTranslation("Hole ID: ")
}

class ChartExporter(private val holeData: HoleData, private val settings: UiSettings) {

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
//      prefWidth = 120
      children = Seq(
//        new Label("Info:"),
        new Label(ChartExporter.VEER_ID + holeData.veerId + "\t\t"),
        new Label(ChartExporter.HOLE_ID + holeData.id)
      )
    }

    val chartProvider = new ChartProvider(holeData, Option.apply(calculateChartWidth()))
    val chartsArea = new VBox() {
      children = chartProvider.getChart(settings.selectedChartType)
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

  private def calculateChartWidth(): Int = {
    val width = ChartExporter.SCREEN_SIZE.width - 10
    width.toInt
  }
}
