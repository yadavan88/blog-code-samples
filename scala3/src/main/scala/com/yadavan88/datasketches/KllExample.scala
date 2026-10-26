package com.yadavan88.datasketches

import org.apache.datasketches.kll.KllDoublesSketch

import java.util.Base64

object KllExample {
  def main(args: Array[String]): Unit = {
    val sketch = KllDoublesSketch.newHeapInstance()

    val latenciesMs = Seq(12.5, 45.2, 102.1, 8.4, 215.0, 33.1, 55.0)
    latenciesMs.foreach(latency => sketch.update(latency))

    // p50 is the median. p99 is the 99th percentile.
    val p50 = sketch.getQuantile(0.50)
    val p99 = sketch.getQuantile(0.99)

    println(s"p50 Latency: $p50 ms")
    println(s"p99 Latency: $p99 ms")

    val base64Value = Base64.getEncoder.encodeToString(sketch.toByteArray)
    println(base64Value)
    // BQEPAsgACAAHAAAAAAAAAMgAAQDBAAAAzczMzMzMIEAAAAAAAOBqQM3MzMzMzCBAAAAAAAAAKUDNzMzMzIxAQJqZmZmZmUZAAAAAAACAS0BmZmZmZoZZQAAAAAAA4GpA
  }
}
