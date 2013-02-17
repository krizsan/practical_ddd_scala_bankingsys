package com.ivan.bank.banking.transactionhistory.valueobjects

import java.util.Date

/**
 * Entry in transaction history of a bank account that describes
 * a balance inquiry on the account.
 * 
 * @author Ivan Krizsan
 */
class BalanceInquiryTransactionHistoryEntry (override val timeStamp : Date,
    override val bankAccountNumber : String) extends
    TransactionHistoryEntry(timeStamp, bankAccountNumber) {
    
}