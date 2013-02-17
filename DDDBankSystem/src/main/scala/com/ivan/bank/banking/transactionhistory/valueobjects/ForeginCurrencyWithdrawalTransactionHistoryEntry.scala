package com.ivan.bank.banking.transactionhistory.valueobjects

import java.util.Date

import com.ivan.bank.domain.valueobjects.Money

/**
 * Entry in transaction history of a bank account that describes
 * a withdrawal of foreign currency from a bank account.
 * 
 * @author Ivan Krizsan
 */
class ForeginCurrencyWithdrawalTransactionHistoryEntry (
    override val timeStamp : Date, override val bankAccountNumber : String,
    val foreignCurrencyAmount : Money, override val amount : Money,
    val exchangeRate : BigDecimal)
    extends WithdrawalTransactionHistoryEntry(timeStamp, bankAccountNumber, amount) {

}