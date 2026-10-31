package com.yadavan88.datasketches

import org.apache.datasketches.hll.{HllSketch, TgtHllType, Union}

object HllMergeExample {
  def main(args: Array[String]): Unit = {
    // Each hour keeps its own sketch. 192.168.1.10 appears in both hours.
    val hour1 = new HllSketch(12, TgtHllType.HLL_4)
    Seq("192.168.1.10", "10.0.0.1").foreach(ip => hour1.update(ip))

    val hour2 = new HllSketch(12, TgtHllType.HLL_4)
    Seq("192.168.1.10", "172.16.0.5").foreach(ip => hour2.update(ip))

    // Merge the two hourly sketches. The shared address is counted once.
    val union = new Union(12)
    union.update(hour1)
    union.update(hour2)
    val merged = union.getResult

    println(s"Hour 1 estimate: ${hour1.getEstimate}")
    println(s"Hour 2 estimate: ${hour2.getEstimate}")
    println(s"Merged estimate: ${merged.getEstimate}")
  }
}
