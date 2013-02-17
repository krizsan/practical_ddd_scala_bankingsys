package com.ivan.bank.banking.transactionhistory.valueobjects

import com.ivan.bank.domain.valueobjects.Money
import java.util.Date

/**
 * Entry in transaction history of a bank account that describes
 * a deposit in the bank account's currency, that is non-foreign currency,
 * to the bank account
 * 
 * @author Ivan Krizsan
 */
class DepositTransactionHistoryEntry (override val timeStamp : Date,
    override val bankAccountNumber : String, val amount : Money) extends
    TransactionHistoryEntry(timeStamp, bankAccountNumber) {
    
}