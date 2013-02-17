package com.ivan.bank.banking.transactionhistory.services

import java.util.Date
import scala.math.BigDecimal.double2bigDecimal
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import com.ivan.bank.banking.services.BankingService
import com.ivan.bank.banking.transactionhistory.interceptors.TransactionHistoryRecorder
import com.ivan.bank.banking.transactionhistory.repositories.TransactionHistoryRepository
import com.ivan.bank.banking.transactionhistory.valueobjects.BalanceInquiryTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.DepositTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.ForeginCurrencyWithdrawalTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.ForeignCurrencyDepositTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.RegistrationTransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.TransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.WithdrawalTransactionHistoryEntry
import com.ivan.bank.common.BankingTestConstants
import com.ivan.bank.domain.entities.BankAccount
import com.ivan.bank.domain.repositories.BankAccountRepository
import com.ivan.bank.domain.valueobjects.Money
import com.ivan.bank.exchangerate.services.ExchangeRateService
import org.scalatest.junit.JUnitRunner

/**
 * Tests the <code>TransactionHistoryService</code>.
 * 
 * @author Ivan Krizsan
 */
@RunWith(classOf[JUnitRunner])
class TransactionHistoryServiceTest extends FunSuite with BeforeAndAfterEach {
    
    /* Field(s): */
    protected var bankingService : BankingService = null
    protected var newBankAccount : BankAccount = null
    protected var transactionHistoryService : TransactionHistoryService = null
    
    override def beforeEach() {
        transactionHistoryService = new TransactionHistoryService()
        bankingService = new BankingService() with TransactionHistoryRecorder
        /*
         * Need to set a reference to the transaction history service
         * required by the transaction history recorder trait.
         * The banking service has no knowledge of this service.
         */
        bankingService.asInstanceOf[TransactionHistoryRecorder].
        	transactionHistoryService = transactionHistoryService
        
        val theExchangeRateService = createExchangeRateService()
        bankingService.exchangeRateService = theExchangeRateService
        
        /* 
         * Need to clear the repositories, as to leave no lingering
         * bank accounts or transaction history entries from earlier tests.
         */
        BankAccountRepository.clear()
        TransactionHistoryRepository.clear()
        
        /* Create a new bank account that has not been registered. */
        newBankAccount = new BankAccount(BankingTestConstants.CURRENCY_TWD)
        newBankAccount.accountNumber = BankingTestConstants.BANK_ACCOUNT_NUMBER
    }
    
    private def createExchangeRateService() : ExchangeRateService = {
        /*
         * Create the exchange rate service and register some
         * exchange rates for known currencies.
         */
        val theExchangeRateService = new ExchangeRateService()
        theExchangeRateService.registerExchangeRate(
            BankingTestConstants.CURRENCY_TWD,
            BankingTestConstants.CURRENCY_SEK,
            BankingTestConstants.EXCHANGERATE_TWD_SEK)
        theExchangeRateService.registerExchangeRate(
            BankingTestConstants.CURRENCY_SEK,
            BankingTestConstants.CURRENCY_TWD,
            BankingTestConstants.EXCHANGERATE_SEK_TWD)
        
       theExchangeRateService
    }
    
    private def changeExchangeRates() : Unit = {
        /*
         * Change the exchange rates, in order to be able to verify
         * that the exchange rates saved in the transaction history
         * are the exchange rates at the time of the transactions and
         * not the current exchange rates.
         */
        bankingService.exchangeRateService.registerExchangeRate(
            BankingTestConstants.CURRENCY_TWD,
            BankingTestConstants.CURRENCY_SEK,
            BankingTestConstants.EXCHANGERATE_TWD_SEK - 1.0)
        bankingService.exchangeRateService.registerExchangeRate(
            BankingTestConstants.CURRENCY_SEK,
            BankingTestConstants.CURRENCY_TWD,
            BankingTestConstants.EXCHANGERATE_SEK_TWD - 1.0)
    }
    
    test("A new bank account should have a registration entry in its " +
    	 "transaction history") {
        bankingService.registerBankAccount(newBankAccount)
        
        val theTransactionHistory : List[TransactionHistoryEntry] =
            transactionHistoryService.retrieveTransactionHistory(
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
        
        /* 
         * Verify the number of entries in the transaction history.
         * Note that the registration of the bank account will always
         * be the first entry in the transaction history.
         */
        assert(theTransactionHistory.size == 1)
        
        /* Verify the type of entry in the transaction history. */
        val theTransactionHistoryEntry = theTransactionHistory.last
        theTransactionHistoryEntry match {
            case _ : RegistrationTransactionHistoryEntry =>
                /* Expected result, do nothing. */
            case _ =>
                assert(false, "Transaction history entry type do not match")
        }
    }
    
    test("A balance inquiry should result in an entry in the transaction " +
    	 "history of the bank account") {
        bankingService.registerBankAccount(newBankAccount)
        bankingService.balance(
            BankingTestConstants.BANK_ACCOUNT_NUMBER)
        
        val theTransactionHistory : List[TransactionHistoryEntry] =
            transactionHistoryService.retrieveTransactionHistory(
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
        
        assert(theTransactionHistory.size == 2)
        
        /*
         * Verify the type of the most recent entry in the transaction
         * history, which should be a balance inquiry.
         */
        val theTransactionHistoryEntry = theTransactionHistory.last
        theTransactionHistoryEntry match {
            case _ : BalanceInquiryTransactionHistoryEntry =>
                /* Expected result, do nothing. */
            case _ =>
                assert(false, "Transaction history entry type do not match")
        }
    }
    
    test("A deposit should result in an entry in the transaction history of " +
    	 "the bank account, with the amount deposited specified") {
        bankingService.registerBankAccount(newBankAccount)
        
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_200_TWD)
        
        val theTransactionHistory : List[TransactionHistoryEntry] =
            transactionHistoryService.retrieveTransactionHistory(
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
        
        assert(theTransactionHistory.size == 2)
        val theTransactionHistoryEntry = theTransactionHistory.last
        theTransactionHistoryEntry match {
            case theDepositEntry : DepositTransactionHistoryEntry =>
                assert(theDepositEntry.amount == BankingTestConstants.MONEY_200_TWD)
            case _ =>
                assert(false, "Transaction history entry type do not match")
        }
    }
    
    test("A deposit of foreign currency should result in the following " +
    	 "additional data being visible in the corresponding entry in the " +
    	 "transaction history of the bank account: " +
    	 "Original currency, amount in original currency, " +
    	 "exchange rate at the time of the deposit") {
        bankingService.registerBankAccount(newBankAccount)
        
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_10_SEK)
        
        changeExchangeRates()
        
        val theTransactionHistory : List[TransactionHistoryEntry] =
            transactionHistoryService.retrieveTransactionHistory(
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
            
        assert(theTransactionHistory.size == 2)
        val theTransactionHistoryEntry = theTransactionHistory.last
        theTransactionHistoryEntry match {
            case theDepositEntry : ForeignCurrencyDepositTransactionHistoryEntry =>
                assert(theDepositEntry.amount == BankingTestConstants.MONEY_40_TWD)
                assert(theDepositEntry.foreignCurrencyAmount ==
                    BankingTestConstants.MONEY_10_SEK)
                assert(theDepositEntry.exchangeRate ==
                    BankingTestConstants.EXCHANGERATE_SEK_TWD)
            case _ =>
                assert(false, "Transaction history entry type do not match")
        }
    }
    
    test("A withdrawal should result in an entry in the transaction history " +
    	 "of the bank account, with the amount withdrawn specified") {
        bankingService.registerBankAccount(newBankAccount)
        
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_200_TWD)
        bankingService.withdraw(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_160_TWD)
        
        val theTransactionHistory : List[TransactionHistoryEntry] =
            transactionHistoryService.retrieveTransactionHistory(
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
            
        assert(theTransactionHistory.size == 3)
        val theTransactionHistoryEntry = theTransactionHistory.last
        theTransactionHistoryEntry match {
            case theWithdrawalEntry : WithdrawalTransactionHistoryEntry =>
                assert(theWithdrawalEntry.amount == BankingTestConstants.MONEY_160_TWD)
            case _ =>
                assert(false, "Transaction history entry type do not match")
        }
    }
    
    test("A withdrawal of foreign currency should result in the following " +
    	 "additional data being visible in the corresponding entry in the " +
    	 "transaction history of the bank account: " +
    	 "Original currency, amount in original currency, " +
    	 "exchange rate at the time of the withdrawal") {
        bankingService.registerBankAccount(newBankAccount)
        
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_200_TWD)
        bankingService.withdraw(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_10_SEK)
            
        changeExchangeRates()
        
        val theTransactionHistory : List[TransactionHistoryEntry] =
            transactionHistoryService.retrieveTransactionHistory(
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
            
        assert(theTransactionHistory.size == 3)
        val theTransactionHistoryEntry = theTransactionHistory.last
        theTransactionHistoryEntry match {
            case theWithdrawalEntry : ForeginCurrencyWithdrawalTransactionHistoryEntry =>
                assert(theWithdrawalEntry.amount == BankingTestConstants.MONEY_40_TWD)
                assert(theWithdrawalEntry.foreignCurrencyAmount ==
                    BankingTestConstants.MONEY_10_SEK)
                assert(theWithdrawalEntry.exchangeRate ==
                    BankingTestConstants.EXCHANGERATE_SEK_TWD)
            case _ =>
                assert(false, "Transaction history entry type do not match")
        }
    }
    
    test("All entries in the bank account transaction history should have a date" +
    	 " and time, have a bank account number and should be ordered in " +
    	 "reverse chronological order") {
        bankingService.registerBankAccount(newBankAccount)
        
        /* Local currency deposit. **/
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_200_TWD)
        /* Foreign currency deposit. */
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_10_SEK)
        /* Local currency withdrawal. */
        bankingService.withdraw(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_50_1_TWD)
        /* Foreign currency withdrawal. */
        bankingService.withdraw(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_10_SEK)
        /* Balance inquiry. */
        bankingService.balance(BankingTestConstants.BANK_ACCOUNT_NUMBER)
        
        val theTransactionHistory : List[TransactionHistoryEntry] =
            transactionHistoryService.retrieveTransactionHistory(
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
            
        assert(theTransactionHistory.size == 6)
        
        val theCurrentDateTime = new Date()
        var thePreviousEntry : TransactionHistoryEntry = null
        /*
         * Closure that compares two dates. Returns true if the first date
         * is not after the second date. That is, the first date is earlier
         * or the same as the second date.
         */
        val isNotAfter = (theFirstDate : Date, theSecondDate : Date) =>
            !theFirstDate.after(theSecondDate)
        
        theTransactionHistory.foreach {
            theEntry =>
            /*
             * The time of this transaction history entry should be
             * before, or same, as a point in time after all transactions.
             */
            assert(isNotAfter(theEntry.timeStamp, theCurrentDateTime))
            /* 
             * This transaction history entry should contain the account
             * number of the bank account involved in the transaction.
             */
            assert(theEntry.bankAccountNumber ==
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
            if (thePreviousEntry != null) {
                /* 
                 * The previous entry should have happened later, or at
                 * the same time, as this entry.
                 */
                assert(isNotAfter(thePreviousEntry.timeStamp, theEntry.timeStamp))
            }
            
            thePreviousEntry = theEntry
        }
    }
    
    test("A request to the banking service that results in an exception " +
    	 "should not generate an entry in the transaction history") ({
        bankingService.registerBankAccount(newBankAccount)
        
        /* Send request to banking service that will cause exception. */
        try {
            val theNegativeMoney = new Money(-1.0,
                BankingTestConstants.CURRENCY_TWD)
        	bankingService.deposit(
        	    BankingTestConstants.BANK_ACCOUNT_NUMBER, theNegativeMoney)
        } catch {
            case _ : Throwable =>
                /* Do nothing. */
        }
        
        val theTransactionHistory : List[TransactionHistoryEntry] =
            transactionHistoryService.retrieveTransactionHistory(
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
            
        assert(theTransactionHistory.size == 1)
    })
}