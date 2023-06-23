package com.yadavan88.scalatestdemo

import org.scalatest.{Outcome, Retries}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.tagobjects.Retryable
import scala.concurrent.duration._

//@Retryable
class FullRetrySpec extends AnyFlatSpec with Retries {
  val maxRetryCount = 5
  override def withFixture(test: NoArgTest): Outcome = {
    println(">>>>>>>>")
    if (isRetryable(test)) {
      withRetry(10.seconds)(super.withFixture(test))
    } else super.withFixture(test)
  }

  var count = 0
  it should "retry this test 3 times and then pass" taggedAs Retryable in {
    println("inside test, count=  "+count)
    count = count + 1
    if (count == 3) succeed else {
      println("this is failure")
      assert(false)
    }
  }

}
