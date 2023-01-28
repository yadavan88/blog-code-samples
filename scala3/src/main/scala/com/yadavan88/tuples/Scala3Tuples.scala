package com.yadavan88.tuples

import scala.runtime.TupleXXL.apply
import scala.runtime.TupleXXL
import scala.deriving.Mirror

object Scala3Tuples {
  @main
  def scala3TuplesMain = {
    val tuple = ("This", "is", "Scala", 3, "Tuple")
    assert(tuple(0) == "This")
    assert(tuple(3) == 3)
    assert(tuple._1 == tuple(0))

    // csv data for tv series data
    // SL No, Series Name, Lead Actor, Lenght, IMDB Ratings
    val csvRow = (1, "Sherlock", "Benedict Cumberbatch", 72, 8.9d)
    assert(csvRow.head == 1) // head to get 1st element
    assert(csvRow.last == 8.9d)
    assert(csvRow.take(2) == (1, "Sherlock"))
    assert((1, 2, 3).zip("A", "B", "C") == ((1, "A"), (2, "B"), (3, "C")))
    assert((1, 2, 3).toList == List(1, 2, 3))

  }

  case class SeriesCSVRow(
      slNo: Int,
      name: String,
      actor: String,
      length: Int,
      rating: Double
  )
}
