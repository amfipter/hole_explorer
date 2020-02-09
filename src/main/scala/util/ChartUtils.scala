package util

import scala.collection.immutable.Range
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object ChartUtils {
  private val GREEN1 = "green"//"#00FF00"
  private val GREEN2 = "olivedrab"//"#06c253"
  private val YELLOW1 = "yellow"//"#ffc33d"
  private val YELLOW2 = "orange"//"#ed9e04"
  private val RED1 = "salmon"//"#ff9090"
  private val RED2 = "red"//"#ff0000"
  private val RED3 = "darkred"//"#c51010"

  private val GREEN1_LIMIT = 1D
  private val GREEN2_LIMIT = 1.1D
  private val YELLOW1_LIMIT = 1.25D
  private val YELLOW2_LIMIT = 1.5D
  private val RED1_LIMIT = 2D
  private val RED2_LIMIT = 3D

  def colorSequenceSimple(seq: Seq[(Double, Double)]): Seq[((Double, Double), String)] = {
    val median = calcMedian(seq.map(pair => pair._2))
    seq.map(value => (value, mapValue(value._2, median)))
  }

  //TODO tests
  def colorSequence(seq: Seq[(Double, Double)]): Seq[((Double, Double), String)] = {
    val median = calcMedian(seq.map(pair => pair._2))
    println("Median: " + median)
    val mapped = seq.map(value => ((value._1, value._2), mapValue(value._2, median)))
    println(mapped)
    val output = new ArrayBuffer[((Double, Double), String)]()
//    output.addOne(mapped(0))

    var initValue = 0D
    var last = mapped(1)
    var accumulatedValue = mapped(1)

    for(i <- (2 until mapped.size)) {
      val current = mapped(i)
      if(current._2 == last._2) {
        accumulatedValue = ((current._1._1 - initValue, current._1._2), last._2)
        if(i == mapped.size - 1) {
          output.addOne(accumulatedValue)
        }
      }
      else {
        output.addOne(accumulatedValue)
        accumulatedValue = ((current._1._1 - last._1._1, current._1._2), current._2)
        if(i == mapped.size - 1) {
          output.addOne(accumulatedValue)
        }
        initValue = last._1._1
      }
      last = current
    }

//    output.map(element => (element._1, element._2)).toSeq
    output.toSeq
  }

  private def mapValue(value: Double, median: Double): String = {
    if(value < GREEN1_LIMIT * median) {
      GREEN1
    }
    else if(value <= GREEN2_LIMIT * median) {
      //not a bug
      GREEN1
    } else if(value <= YELLOW1_LIMIT * median) {
      YELLOW1
    } else if(value <= YELLOW2_LIMIT * median) {
      YELLOW2
    } else if(value <= RED1_LIMIT * median) {
      RED1
    } else if(value <= RED2_LIMIT * median) {
      RED2
    } else
    {
      RED3
    }
  }

  private def calcMedian(seq: Seq[Double]): Double ={
    if(seq.size % 2 == 1) {
      seq.sortWith(_ < _)(seq.size / 2)
    } else {
      val (up, down) = seq.sortWith(_ < _).splitAt(seq.size / 2)
      (up.last + down.head) / 2
    }
  }
}
