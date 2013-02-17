package com.ivan.bank.banking.transactionhistory.repositories

import scala.collection.mutable
import com.ivan.bank.banking.transactionhistory.valueobjects.TransactionHistoryEntry

/**
 * Implements a repository for transaction history entries.
 * The entity identifier for a a list of transaction history entries
 * is a bank account number.
 * Transaction history entries are immutable. Clients receive an
 * immutable list of the actual entries in the repository.
 * 
 * @author Ivan Krizsan
 */
object TransactionHistoryRepository {
    /* Constant(s): */
    
    /* Field(s): */
    private var transactionHistoryEntries :
    	mutable.Map[String, mutable.ListBuffer[TransactionHistoryEntry]] =
    	    mutable.Map()
    
    /**
     * Creates the supplied transaction history entry in the repository.
     * 
     * @param inEntry Transaction history entry which to create in the
     * repository. Must not be null and must contain a bank account number.
     */
    def create(inEntry : TransactionHistoryEntry) : Unit = {
        require(inEntry != null, "cannot insert null entries")
        require(inEntry.bankAccountNumber != null,
            "transaction history entry must contain a bank account number")
        
        val theBankAccountNumber = inEntry.bankAccountNumber
        val theExistingEntries = findOrCreateEntriesForAccount(
            theBankAccountNumber)
        theExistingEntries += inEntry
    }
    
    /**
     * Reads the transaction history for the bank account with the
     * supplied number.
     * 
     * @param inBankAccountNumber Bank account number which transaction
     * history to read.
     * @return Immutable list holding the transaction history of the
     * bank account. Empty list if there is no transaction history.
     */
    def read(inBankAccountNumber : String) : List[TransactionHistoryEntry] = {
        require(inBankAccountNumber != null,
            "cannot retrieve transaction history for null bank account number")
        
        val theExistingEntries = findOrCreateEntriesForAccount(
            inBankAccountNumber)
        theExistingEntries.toList
    }
    
    /**
     * Finds or creates a transaction entry list for the supplied
     * bank account number.
     * 
     * @param inBankAccountNumber Bank account number.
     * @return List of transaction history entries for the bank account number.
     */
    private def findOrCreateEntriesForAccount(inBankAccountNumber : String) :
    	mutable.ListBuffer[TransactionHistoryEntry] = {
        transactionHistoryEntries.getOrElseUpdate(inBankAccountNumber,
            new mutable.ListBuffer[TransactionHistoryEntry])
    }

    /**
     * Clears the repository by removing all transaction history entries
     * for all bank accounts.
     */
    def clear() : Unit = {
        transactionHistoryEntries.clear
    }
}