package com.ivan.bank.banking.factories

import com.ivan.bank.domain.repositories.BankAccountRepository
import org.junit.runner.RunWith
import com.ivan.bank.domain.repositories.BankAccountRepository
import org.scalatest.junit.JUnitRunner
import com.ivan.bank.banking.services.BankingServiceTest
import com.ivan.bank.domain.entities.BankAccount
import com.ivan.bank.common.BankingTestConstants

/**
 * Tests an instance of the banking service, as created by
 * the <code>BankingServiceFactory</code> factory.
 *
 * @author Ivan Krizsan
 */
@RunWith(classOf[JUnitRunner])
class BankingServiceFactoryTest extends BankingServiceTest {

    override def beforeEach() {
        bankingService = BankingServiceFactory.createInstance()
        
        /*
         * Register some known exchange rates with the exchange rate
         * service of the banking service.
         */
        val theExchangeRateService = bankingService.exchangeRateService
        theExchangeRateService.registerExchangeRate(
            BankingTestConstants.CURRENCY_TWD,
            BankingTestConstants.CURRENCY_SEK,
            BankingTestConstants.EXCHANGERATE_TWD_SEK)
        theExchangeRateService.registerExchangeRate(
            BankingTestConstants.CURRENCY_SEK,
            BankingTestConstants.CURRENCY_TWD,
            BankingTestConstants.EXCHANGERATE_SEK_TWD)
        
        /* 
         * Need to clear the repository, as to leave no lingering
         * bank accounts from earlier tests.
         */
        BankAccountRepository.clear()
        
        /* Create a new bank account that has not been registered. */
        newBankAccount = new BankAccount(BankingTestConstants.CURRENCY_TWD)
        newBankAccount.accountNumber = BankingTestConstants.BANK_ACCOUNT_NUMBER
    }
}