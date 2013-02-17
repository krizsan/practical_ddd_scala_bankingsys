package com.ivan.bank.domain.repositories

import org.junit.runner.RunWith
import org.scalatest.{FunSuite, BeforeAndAfterEach}
import com.ivan.bank.domain.entities.BankAccount
import com.ivan.bank.domain.valueobjects.Money
import java.util.Currency
import scala.math.BigDecimal.double2bigDecimal
import org.scalatest.junit.JUnitRunner

/**
 * Tests the bank account repository.
 * 
 * @author Ivan Krizsan
 */
@RunWith(classOf[JUnitRunner])
class BankAccountRepositoryTest extends FunSuite with BeforeAndAfterEach {

    /* Constant(s): */
    private val NEW_BANK_ACCOUNTNUMBER = "0000-0001"
    private val CURRENCY = Currency.getInstance("TWD")
    private val MONEY_100 = new Money(100.0, CURRENCY)
    
    /* Field(s): */
    
    override def beforeEach() {
        BankAccountRepository.clear
    }
    
    test("It should be possible to create a new bank account using an" +
    	 " account number that is not assigned to an existing bank account") {
        val theNewBankAccount = new BankAccount(CURRENCY)
        theNewBankAccount.accountNumber = NEW_BANK_ACCOUNTNUMBER
        BankAccountRepository.create(theNewBankAccount)
        
        /* 
         * The lack of exceptions is taken as a sign that a
         * bank account has been created successfully.
         */
    }
    
    test("It should not be possible to create a bank account using an" +
    	 " account number for which a bank account has already been created") {
        val theFirstBankAccount = new BankAccount(CURRENCY)
        theFirstBankAccount.accountNumber = NEW_BANK_ACCOUNTNUMBER
        
        val theSecondBankAccount = new BankAccount(CURRENCY)
        theSecondBankAccount.accountNumber = NEW_BANK_ACCOUNTNUMBER
        
        /* Create first bank account - should succeed. */
        BankAccountRepository.create(theFirstBankAccount)
        
        /* 
         * Create second bank account with the same account number as
         * the previous one just created. Should fail.
         */
        intercept[AssertionError] {
            BankAccountRepository.create(theSecondBankAccount)
        }
    }
    
    test("It should be possible to retrieve a bank account that has been" +
    	 " created earlier using its account number") {
        val theBankAccount = new BankAccount(CURRENCY)
        theBankAccount.accountNumber = NEW_BANK_ACCOUNTNUMBER
        
        /* Create first bank account - should succeed. */
        BankAccountRepository.create(theBankAccount)
        
        /* Try to retrieve an account with the same bank account number. */
        val theReadBankAccountOption = 
            BankAccountRepository.findBankAccountWithAccountNumber(
                NEW_BANK_ACCOUNTNUMBER)
        assert(theReadBankAccountOption.isDefined)
        assert(NEW_BANK_ACCOUNTNUMBER.equals(
            theReadBankAccountOption.get.accountNumber))
        
        /* The find method should return a new instance of BankAccount. */
        assert(theReadBankAccountOption.get ne theBankAccount)
    }
    
    test("It should not be possible to retrieve a bank account using an" +
    	 " account number for which no bank account has been created") {
        val theReadBankAccountOption =
            BankAccountRepository.findBankAccountWithAccountNumber(
                NEW_BANK_ACCOUNTNUMBER)
        assert(theReadBankAccountOption.isEmpty)
    }
    
    test("It should be possible to update a bank account that has been" +
    	 " created earlier") {
        val theBankAccount = new BankAccount(CURRENCY)
        theBankAccount.accountNumber = NEW_BANK_ACCOUNTNUMBER
        
        /* Create a bank account - should succeed. */
        BankAccountRepository.create(theBankAccount)
        
        /* Set a new balance and update the account. */
        theBankAccount.balance = MONEY_100
        BankAccountRepository.update(theBankAccount)
        
        /* Read the bank account and verify the balance. */
        val theReadBankAccountOption =
            BankAccountRepository.findBankAccountWithAccountNumber(
                NEW_BANK_ACCOUNTNUMBER)
        assert(theReadBankAccountOption.isDefined)
        assert(NEW_BANK_ACCOUNTNUMBER.equals(
            theReadBankAccountOption.get.accountNumber))
        assert(theReadBankAccountOption.get.balance == MONEY_100)
    }
    
    test("It should not be possible to update a bank account that has not" +
    	 " been created earlier") {
        val theBankAccount = new BankAccount(CURRENCY)
        theBankAccount.accountNumber = NEW_BANK_ACCOUNTNUMBER
        intercept[AssertionError] {
            BankAccountRepository.update(theBankAccount)
        }
    }
}