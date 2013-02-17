package com.ivan.bank.banking.transactionhistory.valueobjects

import com.ivan.bank.domain.valueobjects.Money
import java.util.Date

/**
 * Entry in transaction history of a bank account that describes a
 * withdrawal in the bank account's currency, that is non-foreign currency,
 * from the bank account.
 * 
 * @author Ivan Krizsan
 */
class WithdrawalTransactionHistoryEntry(override val timeStamp : Date,
    override val bankAccountNumber : String, val amount : Money) extends
    TransactionHistoryEntry(timeStamp, bankAccountNumber) {

}