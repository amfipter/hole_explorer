import java.io.File

import data.HoleDataParser
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.paint.Color._
import scalafx.scene.layout.{GridPane, HBox}
import scalafx.stage.{FileChooser, Stage}
import scalafx.Includes._
import scalafx.stage.FileChooser.ExtensionFilter

import scala.xml.XML

object MainUI {
  val width = 600
  val height = 400
}


class MainUI(private val stage: Stage) extends Scene {
  lazy val fileChooser = new FileChooser {
    title = "Select xml"
    extensionFilters.add(
      new ExtensionFilter("xml description file", Seq("*.xml"))
    )
  }
  var selectedFile: File = null

  fill = White
  content = new HBox {
    padding = Insets(20)
    children = Seq(
      new Button {
        text = "Select file"
        onAction = handle {
          selectedFile = fileChooser.showOpenDialog(stage)
          update()
        }
      }
    )
  }

  private def update(): Unit = {
    if(selectedFile != null) {
      val data = new HoleDataParser(selectedFile)
    }
  }
}
