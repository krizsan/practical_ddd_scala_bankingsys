package com.ivan.bank.banking.transactionhistory.valueobjects

import java.util.Date

/**
 * Entry in transaction history of a bank account that describes
 * the registration of the bank account.
 * 
 * @author Ivan Krizsan
 */
class RegistrationTransactionHistoryEntry(override val timeStamp : Date,
    override val bankAccountNumber : String) extends TransactionHistoryEntry(
        timeStamp, bankAccountNumber) {

}