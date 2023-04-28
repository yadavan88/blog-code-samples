package com.yadavan88

import org.scalatest.flatspec.AnyFlatSpec

class ManualTest extends AnyFlatSpec {
  it should "run the tests from manual profile" in {
    println("Running this test only from manual profile")
    succeed
  }
}
