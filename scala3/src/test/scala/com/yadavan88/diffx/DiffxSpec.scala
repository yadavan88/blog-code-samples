package com.yadavan88.diffx

import com.softwaremill.diffx.*
import com.softwaremill.diffx.generic.auto.{*, given}
import org.scalatest.flatspec.AnyFlatSpec
import scala.concurrent.duration.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import scala.concurrent.duration.Duration
import scala.util.Random

class DiffxSpec extends AnyFlatSpec {

  it should "compare account case classes without any difference" in {
    val acc1 = Account("acc-1", "Yadu", 2)
    val acc2 = Account("acc-1", "Yadu", 2)
    val diffResult = compare(acc1, acc2)
    assert(diffResult.isIdentical, diffResult.show())
  }

  it should "ignore datetime field from comparison" in {
    val from = Account("acc-1", "Yadu", 2)
    val to = Account("acc-2", "Yadu", 1)
    val txn1 =
      SimpleModels.Transaction("txn1", from, to, 100, LocalDateTime.now)
    val txn2 =
      SimpleModels.Transaction("txn1", from, to, 100, LocalDateTime.now)

    given Diff[SimpleModels.Transaction] =
      Diff.summon[SimpleModels.Transaction].ignore(_.dt)

    val diffResult = compare(txn1, txn2)
    assert(diffResult.isIdentical, diffResult.show())
  }

  it should "compare deeply nested structure ignoring a particular nested field" in {
    def genTxn = Transaction(
      UUID.randomUUID().toString,
      Account("1234", "Yadu", 1),
      Account("6789", "Krishnan", 2),
      100,
      TxnExtras(
        TransferModes.MOBILE,
        Option(
          InternalInfo("cor-id", LocalDateTime.now, "d-2313")
        )
      ),
      LocalDateTime.now(),
      "Fund transfer"
    )

    given Diff[Transaction] = Diff.summon[Transaction].ignore(_.dt).ignore(_.id)

    given Diff[InternalInfo] = Diff.summon[InternalInfo].ignore(_.gatewayTS)

    val txn1 = genTxn
    val txn2 = genTxn
    val diffResult = compare(txn1, txn2)
    assert(diffResult.isIdentical, diffResult.show())
  }

  it should "compare amount values with some error difference (epsilon)" in {
    case class ForexConversion(euros: Double, usd: Double)

    def convertToUSD(euros: Double) = (1 + Random.between(0.05d, 0.07d)) * euros

    val forex1 = ForexConversion(100, convertToUSD(100))
    val forex2 = ForexConversion(100, convertToUSD(100))

    given Diff[ForexConversion] =
      Diff.summon[ForexConversion].modify(_.usd).setTo(Diff.approximate(2d))

    val res = compare(forex1, forex2)
    assert(res.isIdentical, res.show())
  }

  it should "use custom approximation for datetime" in {
    import CustomInstances._
    case class SimpleTransaction(id: String, amount: Int, date: LocalDateTime)
    val dateDiff = new ApproximateDiffForDateTime(50.millis)

    given Diff[SimpleTransaction] =
      Diff.summon[SimpleTransaction].modify(_.date).setTo(dateDiff)

    val s1 = SimpleTransaction("i1", 100, LocalDateTime.now)
    val s2 = SimpleTransaction(
      "i1",
      100,
      LocalDateTime.now().plus(20, ChronoUnit.MILLIS)
    )
    val res = compare(s1, s2)
    assert(res.isIdentical, res.show())
  }

  it should "compare nested fields with collection by ignoring a particular field" in {
    case class Outer(id: Int, inner: Seq[Inner])
    case class Inner(uuid: UUID, value: String)
    val outer1 = Outer(
      100,
      Seq(
        Inner(UUID.randomUUID(), "Value1"),
        Inner(UUID.randomUUID(), "Value2")
      )
    )
    val outer2 = Outer(
      100,
      Seq(
        Inner(UUID.randomUUID(), "Value1"),
        Inner(UUID.randomUUID(), "Value2")
      )
    )

    given Diff[Outer] = Diff.summon[Outer].ignore(_.inner.each.uuid)

    val res = compare(outer1, outer2)
    println(res.show())
  }

  it should "ignore all fields of a particular type" in {
    def randomDateTime = LocalDateTime.now().plusSeconds(Random.nextInt(100))

    case class InnerData(txnTime: Option[LocalDateTime])
    case class AuditLog(info: String, inner: InnerData, date: LocalDateTime)
    case class ComplexData(
        id: Int,
        audit: Option[AuditLog],
        dt: LocalDateTime,
        ts: LocalDateTime
    )
    val complexData1 = ComplexData(
      1,
      Some(AuditLog("asd", InnerData(Some(randomDateTime)), randomDateTime)),
      randomDateTime,
      randomDateTime
    )
    val complexData2 = ComplexData(
      1,
      Some(AuditLog("asd", InnerData(Some(randomDateTime)), randomDateTime)),
      randomDateTime,
      randomDateTime
    )

    given Diff[LocalDateTime] = new Diff[LocalDateTime]:
      override def apply(
          left: LocalDateTime,
          right: LocalDateTime,
          context: DiffContext
      ): DiffResult = DiffResult.Ignored

    given Diff[ComplexData] = Diff.summon[ComplexData]

    val compareRes = compare(complexData1, complexData2)

    assert(compareRes.isIdentical, compareRes.show())

  }

}

object CustomInstances {
  class ApproximateDiffForDateTime(epsilonDur: Duration)
      extends Diff[LocalDateTime] {
    override def apply(
        left: LocalDateTime,
        right: LocalDateTime,
        context: DiffContext
    ): DiffResult = {
      val isWithin =
        ChronoUnit.MILLIS.between(left, right) <= epsilonDur.toMillis
      if (isWithin) {
        IdenticalValue(left)
      } else {
        DiffResultValue(left, right)
      }
    }
  }
}

object SimpleModels {

  case class Transaction(
      id: String,
      from: Account,
      to: Account,
      amount: Int,
      dt: LocalDateTime
  )

}
