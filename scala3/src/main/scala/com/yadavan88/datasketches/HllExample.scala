package com.yadavan88.datasketches

import org.apache.datasketches.hll.{HllSketch, TgtHllType}

import java.util.Base64

object HllExample {
  def main(args: Array[String]): Unit = {
    // lgK trades accuracy for memory. 12 is a common starting value.
    // HLL_4 is the compact form.
    val sketch = new HllSketch(12, TgtHllType.HLL_4)

    val ips = Seq("192.168.1.10", "10.0.0.1", "192.168.1.10", "172.16.0.5")
    ips.foreach(ip => sketch.update(ip))

    println(s"Estimated unique IPs: ${sketch.getEstimate}")
    println(s"Lower bound (95% confidence): ${sketch.getLowerBound(2)}")
    println(s"Upper bound (95% confidence): ${sketch.getUpperBound(2)}")

    // Compact bytes are what you store. Base64 is only for printing them.
    val bytes = sketch.toCompactByteArray
    val base64Value = Base64.getEncoder.encodeToString(bytes)
    println(base64Value) // AgEHDAMIAwDt7e4GLGdSCl0xWA8=

    val restored = HllSketch.heapify(Base64.getDecoder.decode(base64Value))
    println(s"Restored estimate: ${restored.getEstimate}")
  }
}
