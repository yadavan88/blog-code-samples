package com.yadavan88.datetime

import java.time.{Instant, ZoneId}
import scala.util.{Failure, Success, Try}

object TimeZoneComparer {
  def isSameTimeZoneOffset(
      zone1: String,
      zone2: String
  ): Either[String, Boolean] = {
    Try {
      val zoneOffset1 = ZoneId.of(zone1).getRules.getOffset(Instant.now)
      val zoneOffset2 = ZoneId.of(zone2).getRules.getOffset(Instant.now)
      zoneOffset1 == zoneOffset2
    }.toEither.left.map(_ => "Invalid TimeZone")
  }
}
