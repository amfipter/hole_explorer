import scalafx.application.JFXApp
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.Scene
import scalafx.scene.chart.{NumberAxis, ScatterChart, XYChart}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle

object Main extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "Hole Explorer"
    width = 600
    height = 450
    scene = new MainUI(stage)
  }
}
