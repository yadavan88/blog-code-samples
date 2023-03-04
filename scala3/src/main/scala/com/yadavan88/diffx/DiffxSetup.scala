package com.yadavan88.diffx

import com.softwaremill.diffx

import java.time.LocalDateTime
import com.softwaremill.diffx.*
import com.softwaremill.diffx.generic.auto.*

import scala.concurrent.duration.*
import java.time.temporal.ChronoUnit
import java.util.UUID
import scala.concurrent.duration.Duration

case class Transaction(
    id: String,
    from: Account,
    to: Account,
    amount: Int,
    extras: TxnExtras,
    dt: LocalDateTime,
    remark: String
)
case class Account(accountNo: String, accountHolder: String, accType: Int)
case class TxnExtras(
    transferMode: TransferModes,
    internalLogs: Option[InternalInfo]
)
case class InternalInfo(
    correlationId: String,
    gatewayTS: LocalDateTime,
    deviceId: String
)
enum TransferModes {
  case MOBILE, WEB, OFFLINE
}

case class RecentTxns(amount: Int, txnDate: LocalDateTime)

object DiffxSetup extends App {

  import com.softwaremill.diffx.generic.auto.{given, *}
  def genTxn = Transaction(
    UUID.randomUUID().toString,
    Account("1234", "Yadu", 1),
    Account("6789", "Krishnan", 2),
    100,
    TxnExtras(
      TransferModes.MOBILE,
      Option(
        InternalInfo(UUID.randomUUID().toString, LocalDateTime.now, "d-2313")
      )
    ),
    LocalDateTime.now,
    "Fund transfer"
  )

  val recent = Seq(
    RecentTxns(10, LocalDateTime.now.minusHours(1)),
    RecentTxns(12, LocalDateTime.now.minusMinutes(66))
  )
  val recentV2 = recent.map(_.copy(txnDate = LocalDateTime.now))

  val txn1 = genTxn
  val txn2 = genTxn.copy(remark = "Gift")

  /* val ignoredDiffAuto: Diff[Transaction] =
    summon[Diff[Transaction]].ignore(_.dt).ignore(_.remark)*/

  class ApproximateDiffForDateTime(epsilonDur: Duration)
      extends Diff[LocalDateTime] {
    override def apply(
        left: LocalDateTime,
        right: LocalDateTime,
        context: DiffContext
    ): DiffResult = {

      val isWithin =
        ChronoUnit.MILLIS.between(left, right) <= epsilonDur.toMillis
      println("isWithin: " + isWithin)
      println(left)
      println(right)
      if (isWithin) {
        IdenticalValue(left)
      } else {
        DiffResultValue(left, right)
      }
    }
  }
  def approxDateTime(duration: Duration) = new ApproximateDiffForDateTime(
    duration
  )

  implicit val logsDiff: Diff[InternalInfo] = Diff
    .derived[InternalInfo]
    .modify(_.gatewayTS)
    .setTo(approxDateTime(10.millis))

  implicit val d: Diff[Transaction] =
    Diff.derived[Transaction]

  val diffResult = compare(txn1, txn2)
  println("Is identical ? " + diffResult.isIdentical)
  println(diffResult.show())

  case class Person(age: Int, weight: Int)
  case class Organization(peopleList: List[Person])
  val org1 = Organization(List(Person(10, 60), Person(20, 65)))
  val org2 = Organization(List(Person(15, 60), Person(25, 65)))

  // implicit val ppl: Diff[Person] = Diff.derived[Person].ignore(_.age)
  given Diff[Organization] = Diff
    .summon[Organization]
    .modify(_.peopleList)
    .matchByValue(_.weight)

  println(compare(org1, org2).show())
  println(compare(org1, org2).isIdentical)

}
