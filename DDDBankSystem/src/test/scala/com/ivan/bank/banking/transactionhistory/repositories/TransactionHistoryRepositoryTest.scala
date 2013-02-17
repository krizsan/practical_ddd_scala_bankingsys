package com.ivan.bank.banking.transactionhistory.repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import com.ivan.bank.banking.transactionhistory.valueobjects.RegistrationTransactionHistoryEntry
import java.util.Date
import com.ivan.bank.common.BankingTestConstants
import com.ivan.bank.domain.valueobjects.Money
import scala.math.BigDecimal.double2bigDecimal
import com.ivan.bank.common.BankingTestConstants
import com.ivan.bank.banking.transactionhistory.valueobjects.TransactionHistoryEntry
import com.ivan.bank.banking.transactionhistory.valueobjects.DepositTransactionHistoryEntry
import org.scalatest.junit.JUnitRunner

/**
 * Tests the transaction history repository.
 * 
 * @author Ivan Krizsan
 */
@RunWith(classOf[JUnitRunner])
class TransactionHistoryRepositoryTest extends FunSuite with BeforeAndAfterEach {
    /* Constant(s): */
    private val BANK_ACCOUNT_1 = "123.123"
    
    /* Field(s): */
    
    override def beforeEach() {
        TransactionHistoryRepository.clear
    }
    
    test("It should not be possible to insert a transaction history entry" +
    	 " without a bank account number") {
        val theNewEntry = new RegistrationTransactionHistoryEntry(
            new Date(), null)
        
        intercept[IllegalArgumentException] {
            TransactionHistoryRepository.create(theNewEntry)
        }
        
        val theRetrievedEntries : List[TransactionHistoryEntry] =
            TransactionHistoryRepository.read(BANK_ACCOUNT_1)
        assert(theRetrievedEntries.size == 0)
    }
    
    test("It should not be possible to retrieve transaction history data" +
    	 " without supplying a bank account number") {
        intercept[IllegalArgumentException] {
            TransactionHistoryRepository.read(null)
        }
    }
    
    test("It should be possible to create a transaction history entry" +
    	 " for a bank account in the repository") {
        createRegistrationEntry()
    }
    
    test("It should be possible to retrieve an empty list of transaction" +
    	 " history entries for a bank account for which no entries have " +
    	 "been created") {
    	val theRetrievedEntries : List[TransactionHistoryEntry] =
            TransactionHistoryRepository.read(BANK_ACCOUNT_1)
        
        assert(theRetrievedEntries.size == 0)
    }
    
    test("It should be possible to retrieve a list of transaction history" +
    	 " entries for a bank account for which entries have been created") {
        /* Bank account registration. */
        val theRegistrationEntry = createRegistrationEntry()
        
        /* Delay to ensure entries get different timestamp. */
        Thread.sleep(10)
        
        /* Deposit of TWD 100. */
        val theDepositMoney = new Money(100.0, BankingTestConstants.CURRENCY_TWD)
        val theDepositEntry = new DepositTransactionHistoryEntry(
            new Date(), BANK_ACCOUNT_1, theDepositMoney)
        TransactionHistoryRepository.create(theDepositEntry)
        
        val theRetrievedEntries : List[TransactionHistoryEntry] =
            TransactionHistoryRepository.read(BANK_ACCOUNT_1)
        
        assert(theRetrievedEntries.size == 2)
        assert(theRetrievedEntries.head == theRegistrationEntry)
        assert(theRetrievedEntries.last == theDepositEntry)
    }
    
    test("It should not be possible to create a null transaction history" +
    	 " list entry") {
        intercept[IllegalArgumentException] {
            TransactionHistoryRepository.create(null)
        }
        
        val theRetrievedEntries : List[TransactionHistoryEntry] =
            TransactionHistoryRepository.read(BANK_ACCOUNT_1)
        assert(theRetrievedEntries.size == 0)
    }
    
    /**
     * Creates a registration entry for a bank account in the repository
     * under test.
     * 
     * @return The registration entry.
     */
    private def createRegistrationEntry() : TransactionHistoryEntry = {
        val theNewEntry = new RegistrationTransactionHistoryEntry(
            new Date(), BANK_ACCOUNT_1)
        TransactionHistoryRepository.create(theNewEntry)
        
        theNewEntry
    }
}