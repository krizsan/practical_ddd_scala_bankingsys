package com.ivan.bank.banking.transactionhistory.valueobjects

import java.util.Date

/**
 * Base class for the different kinds of entries that can occur
 * in the transaction history of a bank account.
 * Transaction history entries are immutable Value Objects.
 * 
 * @author Ivan Krizsan
 */
abstract class TransactionHistoryEntry(val timeStamp : Date,
    val bankAccountNumber : String){
    
}