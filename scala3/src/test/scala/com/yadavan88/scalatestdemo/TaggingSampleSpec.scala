package com.yadavan88.scalatestdemo

import org.scalatest.Tag
import org.scalatest.flatspec.AnyFlatSpec

object NumericTag extends Tag("Numeric")
object StringTag extends Tag("Str")

class TaggingSampleSpec extends AnyFlatSpec {

  it should "calculate square of a number" taggedAs NumericTag in {
    val num = 10
    val square = num * num
    assert(square == 100)
  }

  it should "calculate square of a number without tagging" in {
    val num = 10
    val square = num * num
    assert(square == 100)
  }

  it should "concatenate 2 string values" taggedAs StringTag in {
    assert("a" + "b" == "ab")
  }

}
