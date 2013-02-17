package com.ivan.bank.banking.transactionhistory.interceptors

import java.util.Date
import com.ivan.bank.banking.services.BankingServiceMixinInterface
import com.ivan.bank.banking.transactionhistory.services.TransactionHistoryService
import com.ivan.bank.banking.transactionhistory.valueobjects.BalanceInquiryTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.DepositTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.ForeginCurrencyWithdrawalTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.ForeignCurrencyDepositTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.RegistrationTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.WithdrawalTransactionHistoryEntry
import com.ivan.bank.domain.entities.BankAccount
import com.ivan.bank.domain.valueobjects.Money

/**
 * Records transaction history for operations on bank accounts
 * that are made through requests to the banking service.
 * Note that all the methods in this trait needs to be abstract, since
 * they overrides methods in the BankingServiceInterface trait that are
 * not implemented in any super type of this trait.
 * 
 * @author Ivan Krizsan
 * @see BankingService, BankingServiceMixinInterface
 */
trait TransactionHistoryRecorder extends BankingServiceMixinInterface {
    /* Constant(s): */
    
    /* Field(s): */
    var transactionHistoryService : TransactionHistoryService = null

    /**
     * Records an entry in the transaction history of the specified
     * bank account in connection to registration of a new bank account.
     * 
     * @param inNewBankAccount New bank account to register with the
     * service.
     * @see BankingService.registerBankAccount
     */
    abstract override def registerBankAccount(inNewBankAccount : BankAccount)
    	: Unit = {
        /* Get transaction history entry timestamp as time of request. */
        val theEntryTime = new Date()
        
        /* Invoke the service to register the new account. */
        super.registerBankAccount(inNewBankAccount)
        
        /*
         * Arriving here, the account was successfully registered and
         * a transaction history entry should be generated.
         */
        val theTransactionHistoryEntry =
            new RegistrationTransactionHistoryEntry(theEntryTime,
                inNewBankAccount.accountNumber)
        transactionHistoryService.addTransactionHistoryEntry(
            theTransactionHistoryEntry)
    }
    
    /**
     * Records an entry in the transaction history of the bank account
     * with the supplied account number in connection to querying for
     * the balance of the account.
     * 
     * @param inBankAccountNumber Account number of bank account for
     * which to inquire for balance.
     * @return Balance of the bank account.
     */
    abstract override def balance(inBankAccountNumber : String) : Money = {
        /* Get transaction history entry timestamp as time of request. */
        val theEntryTime = new Date()
        
        val theAccountBalance = super.balance(inBankAccountNumber)
        
        /*
         * Arriving here, the account balance was successfully inquired and
         * a transaction history entry should be generated.
         */
        val theTransactionHistoryEntry =
            new BalanceInquiryTransactionHistoryEntry(theEntryTime,
                inBankAccountNumber)
        transactionHistoryService.addTransactionHistoryEntry(
            theTransactionHistoryEntry)
        
        /* Return the requested balance of the bank account. */
        theAccountBalance
    }
    
    /**
     * Records an entry in the transaction history of the bank account
     * with the supplied account number in connection to depositing
     * money to the account.
     * 
     * @param inBankAccountNumber Account number of bank account to
     * which to deposit money.
     * @param inDepositAmount Amount of money to deposit to the account.
     */
    abstract override def deposit(inBankAccountNumber : String,
        inDepositAmount : Money) : Unit = {
        /* Get transaction history entry timestamp as time of request. */
        val theEntryTime = new Date()
        /*
         * Get balance of the bank account prior to the deposit so that the
         * exchange rate used in a foreign currency deposit can be
         * calculated without using an external service.
         * Note that super.balance is invoked so that no transaction
         * history is generated for this balance request.
         */
        val thePreDepositBalance = super.balance(inBankAccountNumber)
        
        super.deposit(inBankAccountNumber, inDepositAmount)
        
        /*
         * Get the balance of the bank account after the deposit in order
         * to be able to calculate the exchange rate used in a deposit
         * of foreign currency without using an external service.
         */
        val thePostDepositBalance = super.balance(inBankAccountNumber)
        val theLocalCurrencyDepositAmount =
            thePostDepositBalance.amount - thePreDepositBalance.amount
        
        /*
         * Arriving here, money was successfully deposited and
         * a transaction history entry should be generated.
         */
        if (inDepositAmount.currency == thePreDepositBalance.currency) {
            /* A deposit in the bank account local currency was made. */
            val theTransactionHistoryEntry =
            	new DepositTransactionHistoryEntry(theEntryTime,
            		inBankAccountNumber, inDepositAmount)
            transactionHistoryService.addTransactionHistoryEntry(
            		theTransactionHistoryEntry)
        } else {
            /* Foreign currency was deposited. */
            val theLocalCurrencyDeposit = new Money(
                theLocalCurrencyDepositAmount, thePreDepositBalance.currency)
            /* 
             * Calculate the exchange rate used for the deposit without
             * using any external service.
             */
            val theExchangeRate = theLocalCurrencyDepositAmount /
            	inDepositAmount.amount
            val theTransactionHistoryEntry =
                new ForeignCurrencyDepositTransactionHistoryEntry(
                    theEntryTime, inBankAccountNumber, inDepositAmount,
                    theLocalCurrencyDeposit, theExchangeRate)
            transactionHistoryService.addTransactionHistoryEntry(
            		theTransactionHistoryEntry)
        }
    }
    
    /**
     * Records an entry in the transaction history of the bank account
     * with the supplied account number in connection to withdrawing
     * money from the account.
     * 
     * @param inBankAccountNumber Account number of bank account from
     * which to withdraw money.
     * @param inWithdrawAmount Amount of money to withdraw from the account.
     */
    abstract override def withdraw(inBankAccountNumber : String,
        inWithdrawAmount : Money) : Unit = {
        /* Get transaction history entry timestamp as time of request. */
        val theEntryTime = new Date()
        
        /*
         * Get balance of the bank account prior to the withdrawal so that
         * the exchange rate used in a foreign currency withdrawal can be
         * calculated without using an external service.
         * Note that super.balance is invoked so that no transaction
         * history is generated for this balance request.
         */
        val thePreWithdrawBalance = super.balance(inBankAccountNumber)
        
        super.withdraw(inBankAccountNumber, inWithdrawAmount)
        
        /*
         * Get the balance of the bank account after the withdrawal in order
         * to be able to calculate the exchange rate used in a withdrawal
         * of foreign currency without using an external service.
         */
        val thePostWithdrawBalance = super.balance(inBankAccountNumber)
        val theLocalCurrencyWithdrawAmount =
            thePreWithdrawBalance.amount - thePostWithdrawBalance.amount
        
       /*
         * Arriving here, money was successfully withdrawn and
         * a transaction history entry should be generated.
         */
        if (inWithdrawAmount.currency == thePreWithdrawBalance.currency) {
            /* A withdrawal in the bank account local currency was made. */
            val theTransactionHistoryEntry =
	            new WithdrawalTransactionHistoryEntry(theEntryTime,
	                inBankAccountNumber, inWithdrawAmount)
	        transactionHistoryService.addTransactionHistoryEntry(
	            theTransactionHistoryEntry)
        } else {
            /* Foreign currency was withdrawn. */
            val theLocalCurrencyWithdraw = new Money(
                theLocalCurrencyWithdrawAmount, thePreWithdrawBalance.currency)
            /* 
             * Calculate the exchange rate used for the withdrawal without
             * using any external service.
             */
            val theExchangeRate = theLocalCurrencyWithdrawAmount /
            	inWithdrawAmount.amount
            val theTransactionHistoryEntry =
                new ForeginCurrencyWithdrawalTransactionHistoryEntry(
                    theEntryTime, inBankAccountNumber, inWithdrawAmount,
                    theLocalCurrencyWithdraw, theExchangeRate)
            transactionHistoryService.addTransactionHistoryEntry(
            		theTransactionHistoryEntry)
        }
    }
}