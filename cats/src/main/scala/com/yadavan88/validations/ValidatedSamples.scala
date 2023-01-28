package com.yadavan88
package validations

import cats.implicits._
import cats.data._
import cats.data.Validated._

import java.time.LocalDate

case class TransactionV2(
    fromAccount: String,
    toAccount: String,
    amount: Long,
    scheduleDate: Option[LocalDate]
)

case class Transaction(
    fromAccount: String,
    toAccount: String,
    amount: Long
)

object TransactionValidator {
  def validateAccount(account: String): ValidatedNel[BankValidation, String] = {
    Validated
      .cond(
        account.matches("[0-9]{10}"),
        account,
        InvalidAccount
      )
      .toValidatedNel
  }

  def validateAmount(amount: Long): ValidatedNel[BankValidation, Long] = {
    Validated
      .cond(
        amount > 0,
        amount,
        InvalidAmount
      )
      .toValidatedNel
  }

  def validateMaxAmount(
      amount: Long,
      maxAmount: Long
  ): ValidatedNel[BankValidation, Long] = {
    Validated
      .cond(
        amount <= maxAmount,
        amount,
        MaxAmountLimitCrossed
      )
      .toValidatedNel
  }

  def validateScheduleDate(
      date: Option[LocalDate]
  ): ValidatedNel[BankValidation, Option[LocalDate]] = {
    Validated
      .cond(
        date.isEmpty || date.get.isAfter(LocalDate.now),
        date,
        InvalidScheduleDate
      )
      .toValidatedNel
  }

  def isFromAndToSame(
      fromAccount: String,
      toAccount: String
  ): ValidatedNel[BankValidation, Unit] = {
    Validated
      .cond(
        fromAccount != toAccount,
        (),
        FromAndToAccountCantBeSame
      )
      .toValidatedNel
  }

  def validateTransaction(
      fromAccount: String,
      toAccount: String,
      amount: Long
  ): ValidatedNel[BankValidation, Transaction] = {
    (
      validateAccount(fromAccount),
      validateAccount(toAccount),
      validateAmount(amount)
    ).mapN(Transaction)
  }
}

import TransactionValidator._

object ValidationSamples {

  type ValidationResult[A] = ValidatedNel[BankValidation, A]

  def validateTxn(txn: TransactionV2): ValidationResult[TransactionV2] = {
    (
      validateAccount(txn.fromAccount),
      validateAccount(txn.toAccount),
      validateAmount(txn.amount),
      validateScheduleDate(txn.scheduleDate)
    ).mapN(TransactionV2)
  }

  val validatedPort: Validated[String, Int] = Validated.valid(100)
  val invalidPort: Validated[String, Int] =
    Validated.invalid("Port is not an integer value")

  val fromAccount = "1234567890"
  val toAccount = "1234567890"

  import TransactionValidator._

  def main(args: Array[String]): Unit = {
    val incomingTxn = TransactionV2(
      "0123456789",
      "0123456789",
      1000,
      Some(LocalDate.now.plusDays(1))
    )
    val fieldValidatedTxn = validateTxn(incomingTxn)
    val finalValidatedResult: ValidationResult[TransactionV2] =
      fieldValidatedTxn.productL(
        TransactionValidator.isFromAndToSame(
          incomingTxn.fromAccount,
          incomingTxn.toAccount
        )
      )
    println(finalValidatedResult)

    val amountValidated: ValidationResult[Long] = validateAmount(
      incomingTxn.amount
    ).andThen(amt => validateMaxAmount(amt, 1000))

    val asd = validateAmount(incomingTxn.amount) *> validateMaxAmount(
      incomingTxn.amount,
      1000
    )

    val productValidtor
        : Validated[NonEmptyList[BankValidation], (String, Long)] =
      validateAccount(incomingTxn.toAccount) product validateAmount(
        incomingTxn.amount
      )

    println("productValidtor: " + productValidtor)

    val validValue: Validated[String, Int] = Validated.Valid(100)
    val invalidValue = Validated.invalid("invalid")

    def isValidAccount(acc: String): Validated[List[BankValidation], String] =
      Validated.cond(acc.matches("[0-9]{10}"), acc, List(InvalidAccount))
    def isSameBankAccount(
        acc: String
    ): Validated[List[BankValidation], String] =
      Validated.cond(acc.startsWith("12"), acc, List(NotSameBank))

    import cats.Semigroup
    val bankAccount = "1234567890"
    implicit val strSemiGroup: Semigroup[String] =
      Semigroup.instance[String]((a, b) => a)
    val accVal: Validated[List[BankValidation], String] =
      isValidAccount(bankAccount).combine(isSameBankAccount(bankAccount))
    println(accVal)

    val aValidData: Validated[String, Int] = Valid(100)
    val anInvalidData: Validated[String, Int] = Invalid("Invalid input")
    val aValidData_v2: Validated[String, Int] = 100.valid[String]
    val anInvalidData_v2: Validated[String, Int] = "Invalid input".invalid[Int]

    val acc = "account"
    val amt = -1
    val validatedAcc = Validated.cond(
      acc.matches("[0-9]{10}"),
      acc,
      List("Invalid Account Number")
    )
    val validatedAmount = Validated.cond(amt > 0, amt, List("Invalid Amount"))

    println(validateTransaction("123456789", "1234567890", -1))

    val accountNumbers = List("1234567890", "1234567899", "1234567897", "9999")
    val multiValidated =
      accountNumbers.traverse(TransactionValidator.validateAccount)

    println("Multi account validation result:" + multiValidated)

  }

}
