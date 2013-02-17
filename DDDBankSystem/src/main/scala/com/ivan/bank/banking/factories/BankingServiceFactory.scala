package com.ivan.bank.banking.factories

import com.ivan.bank.banking.services.BankingService
import com.ivan.bank.exchangerate.services.ExchangeRateService
import com.ivan.bank.banking.exceptiontranslation.interceptors.BankingServiceExceptionTranslation
import com.ivan.bank.banking.transactionhistory.interceptors.TransactionHistoryRecorder
import com.ivan.bank.banking.transactionhistory.services.TransactionHistoryService

/**
 * Factory that creates and configures instances of the banking service.
 * 
 * @author Ivan Krizsan
 */
object BankingServiceFactory {

    /**
     * Creates and configures an instance of the banking service.
     * 
     * @returns New instance of the banking service.
     */
    def createInstance() : BankingService = {
        val theTransactionHistoryService = new TransactionHistoryService()
        /*
         * Create a new instance of the banking service.
         * Notice how the exception translation trait is mixed
         * in at creation time and that the dependency to the
         * exception translation trait is hidden from clients
         * of this factory.
         */
        val theNewInstance = new BankingService() with
        	BankingServiceExceptionTranslation
        	with TransactionHistoryRecorder
        
        /*
         * Need to set a reference to the transaction history service
         * required by the transaction history recorder trait.
         * The banking service has no knowledge of this service.
         */
        theNewInstance.asInstanceOf[TransactionHistoryRecorder].
        	transactionHistoryService = theTransactionHistoryService

        /*
         * Create an exchange rate service and inject it into
         * the new banking service.
         * Note that clients of this factory are not required to
         * have a dependency on the exchange rate service.
         * This dependency is encapsulated in the banking service.
         */
        val theExchangeRateService = new ExchangeRateService()
        theNewInstance.exchangeRateService = theExchangeRateService
        
        theNewInstance
    }
}