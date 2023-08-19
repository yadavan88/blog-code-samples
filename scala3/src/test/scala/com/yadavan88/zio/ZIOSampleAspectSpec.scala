package com.yadavan88.zio
import zio.*
import zio.test.*
import zio.test.TestAspect.*

import java.io.IOException
import java.time.LocalDateTime

object ZIOSampleAspectSpec extends ZIOSpecDefault {
  override def spec = suite("ZIOSampleAspectSpec")(
    test("a simple zio effect test without any aspect") {
      val zio = ZIO.succeed("Hello ZIO!")
      println("Inside test")
      assertZIO(zio)(Assertion.equalTo("Hello ZIO!"))
    } @@ repeat(Schedule.recurs(4)),
    // commented out to avoid failure in ci
//    test("retries in case of a failure") {
//      for {
//        num <- ZIO.succeed(scala.util.Random.nextInt)
//        _ = println("number is : "+num)
//        isEven = num % 5 == 0
//      } yield assertTrue(isEven)
//    } @@ retry(Schedule.recurs(4)),
    test("execute a flaky test") {
      for {
        zio <- ZIO.succeed("hello")
      } yield assertTrue(zio == "hello")
    } @@ flaky,
    test("execute a NON flaky test") {
      for {
        zio <- ZIO.succeed("hello")
      } yield assertTrue(zio == "hello")
    } @@ nonFlaky,
    test("before and after aspect") {
      for {
        zio <- ZIO.succeed("hello")
        _ = println("before and after")
      } yield assertTrue(zio == "hello")
    } @@ before(Console.printLine("Before the test")) @@ after(
      Console.printLine("after the test")
    ),
    test("checks performance using timeout") {
      // Thread.sleep(1100) // uncomment this line to see failure
      assertTrue(true)
    } @@ timeout(1.second),
    test("show the time for this test") {
      assertTrue(true)
    } @@ timed,
    test("dont run this test in jvm") {
      assertTrue(true)
    } @@ jsOnly,
    test("a chained test") {
      println("inside chained test")
      Thread.sleep(1000)
      assertTrue(true)
    } @@ repeat(Schedule.recurs(3)) @@ after(
      Console.printLine("after in chained")
    ),
    test("a swapped chained test") {
      println("inside chained test")
      Thread.sleep(1000)
      assertTrue(true)
    } @@ after(
      Console.printLine("after in swapped chained")
    ) @@ repeat(Schedule.recurs(3))
  ) @@ before(
    Console.printLine("should be executed before each test in this suite")
  ) @@ beforeAll(Console.printLine("this is beforeAll")) @@ timed

}
