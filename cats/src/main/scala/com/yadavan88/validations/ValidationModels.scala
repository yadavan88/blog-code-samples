package com.yadavan88
package validations

sealed trait BankValidation {
  def error: String
}

case object InvalidAccount extends BankValidation {
  def error = s"The account number should contain 10 digits"
}

case object InvalidAmount extends BankValidation {
  def error = "The transfer account must be greater than 0"
}

case object MaxAmountLimitCrossed extends BankValidation {
  def error = "The transfer amount is more than the max allowed"
}

case object InvalidScheduleDate extends BankValidation {
  def error = "The schedule date should be a future date"
}

case object FromAndToAccountCantBeSame extends BankValidation {
  def error = "From account should be different from To account"
}

case object NotSameBank extends BankValidation {
  def error = s"The account number is from a different bank"
}
