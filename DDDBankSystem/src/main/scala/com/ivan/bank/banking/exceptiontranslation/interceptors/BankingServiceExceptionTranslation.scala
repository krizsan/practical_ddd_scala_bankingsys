package com.ivan.bank.banking.exceptiontranslation.interceptors

import java.util.Currency

import com.ivan.bank.banking.exceptiontranslation.exceptions.BankAccountAlreadyExists
import com.ivan.bank.banking.exceptiontranslation.exceptions.BankAccountNotFound
import com.ivan.bank.banking.exceptiontranslation.exceptions.BankAccountOverdraft
import com.ivan.bank.banking.exceptiontranslation.exceptions.NoExchangeRateRegistered
import com.ivan.bank.banking.services.BankingServiceMixinInterface
import com.ivan.bank.domain.entities.BankAccount
import com.ivan.bank.domain.valueobjects.Money

/**
 * Implements exception translation for the banking service.
 * Note that all the methods in this trait needs to be abstract, since
 * they overrides methods in the BankingServiceMixinInterface trait that are
 * not implemented in any super type of this trait.
 * Methods in the banking service exposed to clients of the service
 * wraps all unexpected exceptions in Error exceptions, while utility
 * methods only used internally by the service let such exceptions
 * propagate out of the method unwrapped.
 * 
 * @author Ivan Krizsan
 * @see BankingServiceMixinInterface, BankingService
 */
trait BankingServiceExceptionTranslation extends BankingServiceMixinInterface {

    /**
     * Performs exception translation in connection to registration
     * of new bank accounts.
     * 
     * @param inNewBankAccount New bank account to register with the
     * service.
     * @see BankingService.registerBankAccount
     */
    abstract override def registerBankAccount(inNewBankAccount : BankAccount)
    	: Unit = {
        try {
            super.registerBankAccount(inNewBankAccount)
        } catch {
            /* Bank account with supplied account number already exists. */
            case _ : AssertionError =>
                throw new BankAccountAlreadyExists(
                    "Failed to register new bank account. " +
                        "An account with number " +
                        inNewBankAccount.accountNumber +
                        " has already been registered.")
            /* Propagate exception indicating bad parameter(s). */
            case theException : IllegalArgumentException =>
                throw theException
            /* Wrap unexpected exceptions. */
            case theException : Throwable =>
                throw new Error("Failed to register new bank account.",
                    theException)
        }
    }
    
    /**
     * Performs exception translation in connection to querying for
     * the balance of a bank account.
     * 
     * @param inBankAccountNumber Account number of bank account for
     * which to inquire for balance.
     * @return Balance of the bank account.
     */
    abstract override def balance(inBankAccountNumber : String) : Money = {
        try {
            /*
             * Note that the return value of the call to the balance
             * method becomes the result of the try-block, which in
             * turn becomes the result of the entire method.
             */
            super.balance(inBankAccountNumber)
        } catch {
            /* Propagate exception indicating bad parameter(s). */
            case theException : IllegalArgumentException =>
                throw theException
            /* Propagate exception indicating bank account not found. */
            case theException : BankAccountNotFound =>
                throw theException
            /* Wrap unexpected exceptions. */
            case theException : Throwable =>
                throw new Error("Failed to retrieve balance for bank account " +
                    inBankAccountNumber, theException)
        }
    }
    
    /**
     * Performs exception translation in connection to depositing
     * money to a bank account.
     * 
     * @param inBankAccountNumber Account number of bank account to
     * which to deposit money.
     * @param inAmount Amount of money to deposit to the account.
     */
    abstract override def deposit(
        inBankAccountNumber : String, inAmount : Money) : Unit = {
        try {
            super.deposit(inBankAccountNumber, inAmount)
        } catch {
            /* Propagate exception indicating bad parameter(s). */
            case theException : IllegalArgumentException =>
                throw theException
            /* Propagate exception indicating bank account not found. */
            case theException : BankAccountNotFound =>
                throw theException
            /* Propagate exception indicating no exchange rate registered. */
            case theException : NoExchangeRateRegistered =>
                throw theException
            /* Wrap unexpected exceptions. */
            case theException : Throwable =>
                throw new Error("Failed to deposit money to bank account " +
                    inBankAccountNumber, theException)
        }
    }
    
    /**
     * Performs exception translation in connection to withdrawing
     * money from a bank account.
     * 
     * @param inBankAccountNumber Account number of bank account from
     * which to withdraw money.
     * @param inAmount Amount of money to withdraw from the account.
     */
    abstract override def withdraw(
        inBankAccountNumber : String, inAmount : Money) : Unit = {
        try {
            super.withdraw(inBankAccountNumber, inAmount)
        } catch {
            /* Attempted to overdraft bank account. */
            case _ : AssertionError =>
                throw new BankAccountOverdraft(
                    "Bank account: " + inBankAccountNumber +
                        ", amount: " + inAmount)
            /* Propagate exception indicating bad parameter(s). */
            case theException : IllegalArgumentException =>
                throw theException
            /* Propagate exception indicating bank account not found. */
            case theException : BankAccountNotFound =>
                throw theException
            /* Propagate exception indicating no exchange rate registered. */
            case theException : NoExchangeRateRegistered =>
                throw theException
            /* Wrap unexpected exceptions. */
            case theException : Throwable =>
                throw new Error("Failed to withdraw money from bank account " +
                    inBankAccountNumber, theException)
        }
    }
    
    /**
     * Performs exception translation in connection to retrieval
     * of existing bank accounts.
     * Also ensures that a bank account was obtained.
     * 
     * @param inBankAccountNumber Account number of bank account
     * to retrieve.
     * @return Option holding bank account with supplied account number.
     */
    abstract override protected def retrieveBankAccount(
        inBankAccountNumber : String) : Option[BankAccount] = {
        try {
            val theBankAccountOption = super.retrieveBankAccount(
                    inBankAccountNumber)
            /* Throw an exception if no bank account found. */
            if (theBankAccountOption.isEmpty) {
                throw new BankAccountNotFound("Unable to find bank account "
                    + inBankAccountNumber)
            }
            theBankAccountOption   
        } catch {
            /* Propagate unexpected exceptions. */
            case theException : Throwable =>
                throw theException
        }
    }
    
    /**
     * Performs exception translation in connection to calculating
     * money exchange.
     * Also ensures that the exchange calculation succeeded and a
     * result was obtained.
     * 
     * @param inAmount Money to exchange.
     * @param inToCurrency Currency to exchange money to.
     * @return Exchanged money.
     */
    abstract override protected def exchangeMoney(
        inAmount : Money, inToCurrency : Currency) : Option[Money] = {
        try {
            val theExchangedMoneyOption = super.exchangeMoney(
                inAmount, inToCurrency)
            if (theExchangedMoneyOption.isEmpty) {
                throw new NoExchangeRateRegistered(
                    "No exchangerate registered for exchange from " +
                    inAmount.currency.toString() + " to " +
                    inToCurrency.toString())
            }
            theExchangedMoneyOption
        } catch {
            /* Propagate unexpected exceptions. */
            case theException : Throwable =>
                throw theException
        }
    }
}