package com.yadavan88.scalatestdemo

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.tagobjects.Retryable
import org.scalatest.{Canceled, Failed, Outcome, Retries}

class RetryWithTaggingSpec extends AnyFlatSpec with Retries {
  val maxRetryCount = 5
  override def withFixture(test: NoArgTest): Outcome = {
    if (isRetryable(test)) {
      withRetry(withFixture(test, maxRetryCount))
    } else super.withFixture(test)
  }

  def withFixture(test: NoArgTest, count: Int): Outcome = {
    val outcome = super.withFixture(test)
    outcome match {
      case Failed(_) | Canceled(_) =>
        if (count == 1) super.withFixture(test)
        else withFixture(test, count - 1)
      case other => other
    }
  }

  var count = 0
  it should "retry this test 3 times and then pass" taggedAs Retryable in {
    count = count + 1
    if (count == 3) succeed
    else {
      println("this is failure")
      assert(false)
    }
  }

  /** This test will always fail as it doesn't retry at all * */
  //  var count_v2 = 0
  //  it should "not retry this test as it is not tagged" in {
  //    count_v2 = count_v2 + 1
  //    if (count_v2 == 3) succeed
  //    else {
  //      println("this is failure from test 2")
  //      assert(false)
  //    }
  //  }

}
