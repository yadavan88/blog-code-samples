package com.yadavan88.datetime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class TimeZoneComparerSpec extends AnyFlatSpec with TableDrivenPropertyChecks {

  val timezoneTable = Table(
    ("timezone1", "timezone2", "Is Same Offset"),
    ("Europe/Paris", "Europe/Berlin", Right(true)),
    ("Europe/Paris", "Europe/London", Right(false)),
    ("Europe/Paris", "Europe/London", Right(false)),
    ("Europe/Bucharest", "Europe/Berlin", Right(false)),
    ("Asia/Kolkata", "Asia/Colombo", Right(true)),
    ("Asia/Kolkata", "Asia/Dhaka", Right(false)),
    ("Europe/Berlin", "Europe/Amsterdam", Right(true)),
    ("wrong-1", "wrong-2", Left("Invalid TimeZone")),
    ("Europe/Berlin", "wrong-2", Left("Invalid TimeZone"))
  )

  "TimeZoneComparerSpec" should "compare two timezone identifier and check if they have same offset" in {
    forAll(timezoneTable) { (timezone1, timezone2, isSameOffset) =>
      assert(
        TimeZoneComparer.isSameTimeZoneOffset(
          timezone1,
          timezone2
        ) == isSameOffset
      )
    }
  }
}
