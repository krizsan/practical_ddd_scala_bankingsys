package com.ivan.bank.banking.services

import org.junit.runner.RunWith
import org.scalatest.{FunSuite, BeforeAndAfterEach}
import com.ivan.bank.domain.entities.BankAccount
import com.ivan.bank.banking.exceptiontranslation.exceptions.BankAccountAlreadyExists
import com.ivan.bank.domain.repositories.BankAccountRepository
import com.ivan.bank.common.BankingTestConstants
import com.ivan.bank.exchangerate.services.ExchangeRateService
import com.ivan.bank.banking.exceptiontranslation.exceptions.BankAccountNotFound
import com.ivan.bank.banking.exceptiontranslation.interceptors.BankingServiceExceptionTranslation
import com.ivan.bank.banking.exceptiontranslation.exceptions.BankAccountOverdraft
import com.ivan.bank.banking.exceptiontranslation.exceptions.NoExchangeRateRegistered
import com.ivan.bank.domain.repositories.BankAccountRepository
import org.scalatest.junit.JUnitRunner

/**
 * Tests the <code>BankingService</code> class.
 * The functionality of the banking service is also tested when
 * the <code>BankingServiceFactory</code> is tested, using the tests
 * in this class. If, in the future, there are additional changes related to
 * the creation of the banking service, one may want to consider
 * making this class an abstract base class of the banking service
 * factory test class or move the testing code in this class to the
 * banking service factory test class.
 * In the latter case, this class can then be removed altogether.
 *
 * @author Ivan Krizsan
 */
@RunWith(classOf[JUnitRunner])
class BankingServiceTest extends FunSuite with BeforeAndAfterEach {
    /* Constant(s): */
    
    /* Field(s): */
    protected var bankingService : BankingService = null
    protected var newBankAccount : BankAccount = null
    
    override def beforeEach() {
        bankingService = new BankingService() with
        	BankingServiceExceptionTranslation
        
        val theExchangeRateService = createExchangeRateService()
        bankingService.exchangeRateService = theExchangeRateService
        
        /* 
         * Need to clear the repository, as to leave no lingering
         * bank accounts from earlier tests.
         */
        BankAccountRepository.clear()
        
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
    
    test("It should be possible to create a new bank account with " +
    	 "an account number that has not previously been used") {
        bankingService.registerBankAccount(newBankAccount)
    }
    
    test("It should not be possible to create a bank account with " +
    	 "an account number that previously has been used") {
        bankingService.registerBankAccount(newBankAccount)
        intercept[BankAccountAlreadyExists] {
            bankingService.registerBankAccount(newBankAccount)
        }
    }
    
    test("It should not be possible to create a bank account with an " +
    	 "account number that is of illegal format") {
        val theBankAccountWithBadAccountNumber = new BankAccount(
            BankingTestConstants.CURRENCY_TWD)
        theBankAccountWithBadAccountNumber.accountNumber =
            BankingTestConstants.BANK_ACCOUNT_NUMBER_BAD_FORMAT
        
        intercept[IllegalArgumentException] {
            bankingService.registerBankAccount(theBankAccountWithBadAccountNumber)
        }
    }
    
    test("It should be possible to perform a balance inquiry on an existing" +
    	 " bank account") {
        bankingService.registerBankAccount(newBankAccount)
        val theBalance = bankingService.balance(
            BankingTestConstants.BANK_ACCOUNT_NUMBER)
        
        assert(theBalance == BankingTestConstants.MONEY_0_TWD)
    }
    
    test("It should not be possible to perform a balance inquiry " +
    	 "using an account number for which there is no bank account") {
        intercept[BankAccountNotFound] {
            val theBalance = bankingService.balance(
                BankingTestConstants.BANK_ACCOUNT_NUMBER)
        }
    }
    
    test("When money is deposited to a bank account, the account balance " +
    	 "should increase accordingly") {
        bankingService.registerBankAccount(newBankAccount)
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_100_3_TWD)
        val theBalance = bankingService.balance(
            BankingTestConstants.BANK_ACCOUNT_NUMBER)
        
        assert(theBalance == BankingTestConstants.MONEY_100_3_TWD)
    }
    
    test("It should not be possible to deposit money using an " +
    	 "account number for which there is no bank account") {
        intercept[BankAccountNotFound] {
            bankingService.deposit(
                BankingTestConstants.BANK_ACCOUNT_NUMBER,
                BankingTestConstants.MONEY_100_3_TWD)
        }
    }
    
    test("When money is withdrawn from a bank account, the " +
    	 "account balance should decrease accordingly") {
        bankingService.registerBankAccount(newBankAccount)
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_100_3_TWD)
        bankingService.withdraw(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_50_1_TWD)
        val theBalance = bankingService.balance(
            BankingTestConstants.BANK_ACCOUNT_NUMBER)
        
        assert(theBalance == BankingTestConstants.MONEY_50_2_TWD)
    }
    
    test("It should not be possible to withdraw money using an " +
    	 "account number for which there is no bank account") {
        intercept[BankAccountNotFound] {
            bankingService.withdraw(
                BankingTestConstants.BANK_ACCOUNT_NUMBER,
                BankingTestConstants.MONEY_50_1_TWD)
        }
    }
    
    test("It should not be possible to overdraft a bank account") {
        bankingService.registerBankAccount(newBankAccount)
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_100_3_TWD)
        intercept[BankAccountOverdraft] {
             bankingService.withdraw(
                 BankingTestConstants.BANK_ACCOUNT_NUMBER,
                 BankingTestConstants.MONEY_200_TWD)
         }
        val theBalance = bankingService.balance(
            BankingTestConstants.BANK_ACCOUNT_NUMBER)
        assert(theBalance == BankingTestConstants.MONEY_100_3_TWD)
    }
    
    test("When money in a recognized currency that is not the bank " +
    	 "account's currency is deposited to a bank account, the " +
    	 "account balance should increase by the corresponding amount " +
    	 "in the bank account's currency calculated using the appropriate " +
    	 "buy exchange rate") {
        /*
         * A currency is considered foreign if it is not same as 
         * the currency of the bank account to which the money in
         * that currency deposited or withdrawn.
         */
        bankingService.registerBankAccount(newBankAccount)
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_10_SEK)
        val theBalance = bankingService.balance(
            BankingTestConstants.BANK_ACCOUNT_NUMBER)
    	assert(theBalance == BankingTestConstants.MONEY_40_TWD)
    }
    
    test("When money in a recognized currency that is not the bank " +
    	 "account's currency is withdrawn from a bank account, the " +
    	 "account balance should decrease by the corresponding amount " +
    	 "in the bank account's currency calculated using the appropriate " +
    	 "sell exchange rate") {
        /*
         * A currency is considered foreign if it is not same as 
         * the currency of the bank account to which the money in
         * that currency deposited or withdrawn.
         */
        bankingService.registerBankAccount(newBankAccount)
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_200_TWD)
        bankingService.withdraw(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_10_SEK)
        
        val theBalance = bankingService.balance(
            BankingTestConstants.BANK_ACCOUNT_NUMBER)
    	assert(theBalance == BankingTestConstants.MONEY_160_TWD)
    }
    
    test("It should not be possible to deposit money in a currency for " +
    	 "which no exchange rate has been registered") {
        /*
         * A currency is considered foreign if it is not same as 
         * the currency of the bank account to which the money in
         * that currency deposited or withdrawn.
         */
    	bankingService.registerBankAccount(newBankAccount)
    	
    	intercept[NoExchangeRateRegistered] {
    	    bankingService.deposit(
    	        BankingTestConstants.BANK_ACCOUNT_NUMBER,
    	        BankingTestConstants.MONEY_10_USD_NOTREGISTERED)
    	}
    	val theBalance = bankingService.balance(
    	    BankingTestConstants.BANK_ACCOUNT_NUMBER)
    	assert(theBalance == BankingTestConstants.MONEY_0_TWD)
    }
    
    test("It should not be possible to withdraw money in a currency for " +
    	 "which no exchange rate has been registered") {
        /*
         * A currency is considered foreign if it is not same as 
         * the currency of the bank account to which the money in
         * that currency deposited or withdrawn.
         */
        bankingService.registerBankAccount(newBankAccount)
        bankingService.deposit(
            BankingTestConstants.BANK_ACCOUNT_NUMBER,
            BankingTestConstants.MONEY_200_TWD)
        
        intercept[NoExchangeRateRegistered] {
            bankingService.withdraw(
                BankingTestConstants.BANK_ACCOUNT_NUMBER,
                BankingTestConstants.MONEY_10_USD_NOTREGISTERED)
        }
        val theBalance = bankingService.balance(
            BankingTestConstants.BANK_ACCOUNT_NUMBER)
    	assert(theBalance == BankingTestConstants.MONEY_200_TWD)
    }
}