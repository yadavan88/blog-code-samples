package com.yadavan88.ulid

import wvlet.airframe.ulid.ULID

object ULIDSample extends App {

  val ulid: ULID = ULID.newULID
  println(ulid)
  println(ulid.epochMillis)
  println(ulid.randomness)
  // for example: 01GNKVTMZ6AW7D1FP7QHQ2E86Y

  println(ulid.epochMillis)

  val ulidStr: String = ULID.newULIDString
  println(ulidStr)

  val ulidFromStr = ULID.fromString(ulidStr)
  println(ulidFromStr)

  println("------------------------------")
  val ulids: List[(Int, ULID)] = (1 to 10).toList.map { i =>
    (i, ULID.newULID)
  }
  ulids.foreach(println)

  // verify that same ulids are not generated
  assert(ulids.map(_._2).toSet.size == 10)
  // verify monotonic ordering
  assert(ulids.sortBy(_._2) == ulids)
  // see if ulids generated within same millisecond
  val diffMillis = ulids.map(_._2).map(_.epochMillis).toSet.size
  if (diffMillis == 10) {
    println("All ULIDs generated in different milliseconds")
  } else {
    println(
      s"There are some overlapping milliseconds for ULIDs. ${diffMillis} ULIDs generated within same millis"
    )
  }

}
