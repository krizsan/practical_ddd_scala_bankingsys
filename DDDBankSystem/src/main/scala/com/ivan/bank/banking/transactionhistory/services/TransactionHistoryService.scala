package com.ivan.bank.banking.transactionhistory.services

import com.ivan.bank.banking.transactionhistory.repositories.TransactionHistoryRepository
import com.ivan.bank.banking.transactionhistory.valueobjects.TransactionHistoryEntry

/**
 * Allows for storage and retrieval of transaction history entries for
 * bank accounts.
 * 
 * @author Ivan Krizsan
 */
class TransactionHistoryService {

    /**
     * Retrieves the transaction history for the bank account with
     * the supplied account number.
     * 
     * @param inBankAccountNumber Account number of bank account which
     * transaction history to retrieve.
     * @return Chronologically ordered list containing the bank account's
     * transaction history.
     */
    def retrieveTransactionHistory(inBankAccountNumber : String) :
    	List[TransactionHistoryEntry] = {
        TransactionHistoryRepository.read(inBankAccountNumber)
    }
    
    /**
     * Adds the supplied transaction history entry to the transaction
     * history entries of the bank account which account number is
     * specified in the entry.
     * 
     * @param inTransactionHistoryEntry Transaction history entry to be
     * added.
     */
    def addTransactionHistoryEntry(
        inTransactionHistoryEntry : TransactionHistoryEntry) : Unit = {
        TransactionHistoryRepository.create(inTransactionHistoryEntry)
    }
}