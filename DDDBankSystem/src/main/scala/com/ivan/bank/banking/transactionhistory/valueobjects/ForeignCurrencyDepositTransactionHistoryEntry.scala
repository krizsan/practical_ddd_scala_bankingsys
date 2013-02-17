package com.ivan.bank.banking.transactionhistory.valueobjects

import com.ivan.bank.domain.valueobjects.Money
import java.util.Date

/**
 * Entry in transaction history of a bank account that describes a
 * deposit of foreign currency to the bank account.
 * 
 * @author Ivan Krizsan
 */
class ForeignCurrencyDepositTransactionHistoryEntry (
    override val timeStamp : Date, override val bankAccountNumber : String,
    val foreignCurrencyAmount : Money, override val amount : Money,
    val exchangeRate : BigDecimal) extends
    DepositTransactionHistoryEntry(timeStamp, bankAccountNumber, amount) {
    
}