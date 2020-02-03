package data

import data.UiSettings.SelectedChartType

object UiSettings {
  object SelectedChartType extends Enumeration {
    val SEPARATE, INTEGRATE = Value
  }
}

class UiSettings {
  var skipFirstValue = true
  var selectedChartType = SelectedChartType.SEPARATE
  var selectedHoleId: Option[Int] = Option.empty
}
