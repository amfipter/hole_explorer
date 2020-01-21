package localizer

object Localizer {
  private val TRANSLATOR = collection.immutable.Map.newBuilder[String, String]
    .addOne("Depth", "Глубина")
    .addOne("Value", "Значение")
    .addOne("Additional options", "Дополнительные параметры")
    .addOne("Penetration rate chart", "Диаграмма скорости проходки")
    .addOne("Veer ID: ", "Веер: ")
    .addOne("Hole ID: ", "Скважина: ")
    .addOne("Export charts", "Экспорт")
    .addOne("Nothing to export", "Нечего экспортировать")
    .addOne("Select xml", "Выбор xml файла")
    .addOne("Export", "Экспорт")
    .addOne("Select file", "Выбор файла")
    .addOne("PercPressure", "Давление ударного действия")
    .addOne("FeedPressure", "Давление подачи")
    .addOne("RotPressure", "Давление вращения")
    .addOne("FlushPressure", "Давление промывки")
    .addOne("PenetrRate", "Скорость проходки")
    .addOne("DepthTag", "Отметка глубины")
    .result()

  def getTranslation(literal: String): String = {
    TRANSLATOR.getOrElse(literal, literal)
  }
}
