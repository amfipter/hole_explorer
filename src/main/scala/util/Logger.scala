package util

import java.util.Date

object Logger {
  def log(message: String): Unit = {
    logPrint(message + "\n")
  }

  def logWithTrace(message: String) = {
    logPrint(message)
    logPrint("\n")
    logPrint(System.currentTimeMillis())
    logPrint(Thread.currentThread().getStackTrace)
  }

  private def logPrint(x: Any): Unit = {
    //TODO
    //Default output
    print(x)
  }
}
