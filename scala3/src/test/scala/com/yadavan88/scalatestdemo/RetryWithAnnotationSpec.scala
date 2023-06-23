package com.yadavan88.scalatestdemo

import org.scalatest.Retries.{isRetryable, withRetry}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.tags.Retryable
import org.scalatest.time.Span
import org.scalatest.{Canceled, Failed, Outcome}

import scala.concurrent.duration._
@Retryable
class RetryWithAnnotationSpec extends AnyFlatSpec {
  val maxRetryCount = 5
  val delay: Span = 5.seconds
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
  it should "[Retryable annotation] retry this test 3 times and then pass" in {
    count = count + 1
    if (count == 3) succeed
    else {
      println("this is failure")
      assert(false)
    }
  }

  var count_v2 = 0
  it should "[Retryable annotation] retry this test also 3 times and then pass" in {
    count_v2 = count_v2 + 1
    if (count_v2 == 3) succeed
    else {
      println("this is failure v2")
      assert(false)
    }
  }

}
