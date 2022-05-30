package com.yadavan88.validations
import cats.data._
import cats.data.Validated._
import cats.implicits._

class ValidationSpec extends munit.FunSuite {

  test("a simple valid data") {
    val intValid: Validated[String, Int] = 10.valid[String]
    assert(intValid.isValid)
  }

  test("a simple invalid data") {
    val intValid: Validated[String, Int] = "Invalid".invalid[Int]
    assert(intValid.isInvalid)
  }

  test("return a valid transaction if all fields are valid") {
    val validatedTxn =
      TransactionValidator.validateTransaction("1234567890", "9876543210", 100)
    assert(validatedTxn.isValid)
  }

  test("return error if account number is not proper") {
    val validatedTxn =
      TransactionValidator.validateTransaction("999", "9876543210", 100)
    assert(validatedTxn.isInvalid)
    assert(validatedTxn.toEither.leftMap(identity) == Left(NonEmptyList.of(InvalidAccount)))
  }

  test("return error if account number and amount is not proper") {
    val validatedTxn =
      TransactionValidator.validateTransaction("999", "9876543210", -100)
    assert(validatedTxn.isInvalid)
    assert(validatedTxn.toEither.leftMap(identity) == Left(NonEmptyList.of(InvalidAccount, InvalidAmount)))
  }

  test("check for same bank account for to and from accounts") {
      val from = "9876543210"
    val validatedTxn =
      TransactionValidator.validateTransaction(from, from, 100)
    assert(validatedTxn.isValid)
    val sameAccValidation = validatedTxn <* TransactionValidator.isFromAndToSame(from, from)
    assert(sameAccValidation.isInvalid)
    val errors = sameAccValidation.fold(e => e.toList, s => Nil)
    assert(errors == List(FromAndToAccountCantBeSame))
  }

}
