package com.yadavan88.ulid

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import wvlet.airframe.ulid.ULID
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ULIDTest extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {
  it should "Check for ULID sorting" in {
    forAll(ULID.newULID, ULID.newULID) { (ulid1, ulid2) =>
      assert(ulid1.<(ulid2))
    }
  }
}
