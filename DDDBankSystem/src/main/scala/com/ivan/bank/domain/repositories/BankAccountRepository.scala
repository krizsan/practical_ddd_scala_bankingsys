package com.ivan.bank.domain.repositories

import scala.collection.mutable
import com.ivan.bank.domain.entities.BankAccount

/**
 * Implements a repository for bank accounts.
 * The entity identifier for a bank account is always the account
 * number, which must be unique.
 * The repository retains copies of bank accounts so any bank account
 * supplied by a client may be freely modified without affecting the
 * contents of the repository.
 * 
 * @author Ivan Krizsan
 */
object BankAccountRepository {
    
    private var accounts : mutable.Map[String, BankAccount] = mutable.Map()

    /**
     * Creates a persisted bank account using the supplied bank account
     * object.
     * 
     * @param inNewBankAccount Bank account to be persisted.
     */
    def create(inNewBankAccount : BankAccount) : Unit = {
        /* Throws IllegalArgumentException if no bank account supplied. */
        require(inNewBankAccount != null, "a bank account must be supplied")
        /* 
         * Throws AssertionError if bank account with the same account
         * number as the new bank account already exists.
         */
        assume(accounts.contains(inNewBankAccount.accountNumber) == false,
            "a bank account with the account number " +
            inNewBankAccount.accountNumber + " already exists")
        
        val theNewBankAccount = inNewBankAccount.clone
        accounts(theNewBankAccount.accountNumber) = theNewBankAccount
    }
    
    /**
     * Finds a bank account with the supplied account number.
     * 
     * @param inBankAccountNumber Account number of bank account to find.
     * @return Bank account with supplied account number, or None if no such
     * account found.
     */
    def findBankAccountWithAccountNumber(inBankAccountNumber : String) :
        Option[BankAccount] = {
        /* Throws IllegalArgumentException if no bank account number supplied. */
        require(inBankAccountNumber != null, "a bank account number is required")
        
        var theBankAccountOption = accounts.get(inBankAccountNumber)
        if (theBankAccountOption.isDefined) {
            /* 
             * Clone the bank account, so that an instance in the
             * repository is never returned to a client.
             */
            val theClonedBankAccount = theBankAccountOption.get.clone
            theBankAccountOption = new Some(theClonedBankAccount)
        }
        theBankAccountOption
    }
    
    /**
     * Updates a bank account that has previously been persisted.
     * The bank account that is to be updated must exist in the
     * repository.
     * 
     * @param inBankAccount Bank account to update.
     */
    def update(inBankAccount : BankAccount) : Unit = {
        /* Throws IllegalArgumentException if no bank account supplied. */
        require(inBankAccount != null, "a bank account must be supplied")
        /* 
         * Throws AssertionError if bank account to update does not exist
         * in the repository.
         */
        assume (accounts.contains(inBankAccount.accountNumber),
            "cannot update bank account with account number " +
            inBankAccount.accountNumber + " since it does not exist in the repository")
        
        val theBankAccount = inBankAccount.clone
        accounts(theBankAccount.accountNumber) = theBankAccount
    }
    
    /**
     * Clears the repository by removing all bank accounts contained in
     * the repository.
     */
    def clear() : Unit = {
        accounts.clear
    }
}