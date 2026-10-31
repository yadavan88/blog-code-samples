package com.yadavan88.datasketches

import org.apache.datasketches.common.ArrayOfStringsSerDe
import org.apache.datasketches.frequencies.{ErrorType, ItemsSketch}

import java.util.Base64

object ItemsExample {
  def main(args: Array[String]): Unit = {
    // How many distinct items to track. Must be a power of two.
    val sketch = new ItemsSketch[String](1024)

    val requests = Seq("DE", "IN", "US", "DE", "DE", "US", "FR", "DE")
    requests.foreach(country => sketch.update(country, 1L))

    // Items the sketch is willing to call frequent.
    val frequentItems = sketch.getFrequentItems(ErrorType.NO_FALSE_POSITIVES)

    frequentItems.foreach { row =>
      println(s"Country: ${row.getItem} | Estimate: ${row.getEstimate}")
    }
    val serde = new ArrayOfStringsSerDe
    val base64Value =
      Base64.getEncoder.encodeToString(sketch.toByteArray(serde))
    println(base64Value)
    // BAEKCgMAAAAEAAAAAAAAAAgAAAAAAAAAAAAAAAAAAAACAAAAAAAAAAEAAAAAAAAAAQAAAAAAAAAEAAAAAAAAAAIAAABVUwIAAABJTgIAAABGUgIAAABERQ==

  }
}
