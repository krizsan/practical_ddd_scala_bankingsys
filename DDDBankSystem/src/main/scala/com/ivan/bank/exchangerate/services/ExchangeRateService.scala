/**
 *
 */
package com.ivan.bank.exchangerate.services

import java.util.Currency
import scala.collection.mutable
import com.ivan.bank.domain.valueobjects.Money

/**
 * Service that, given an exchange rate between two currencies, calculates
 * the value of money in one currency in the other currency.
 * 
 * @author machine
 */
class ExchangeRateService {
	/* Constant(s): */
    
    /* Field(s): */
    private val exchangeRateMap : mutable.Map[(Currency, Currency), BigDecimal] =
        mutable.Map()

    /**
     * Registers the supplied exchange rate to be used when calculating
     * the value of money in supplied from-currency in supplied to-currency.
     * If an exchange rate for the supplied currencies in the specified
     * exchange direction has already been registered, the new exchange
     * rate will replace the previous exchange rate.
     * 
     * @param inFromCurrency Exchange from-currency.
     * @param inToCurrency Exchange to-currency.
     * @param inExchangeRate Exchange rate for specified currency exchange.
     */
    def registerExchangeRate(inFromCurrency : Currency, inToCurrency : Currency,
        inExchangeRate : BigDecimal) : Unit = {
        /* Command-type method that alters state and do not return a result. */
        
        val theCurrencyDirection = (inFromCurrency, inToCurrency)
        exchangeRateMap.put(theCurrencyDirection, inExchangeRate)
    }
    
    /**
     * Calculates the value of supplied amount of money in the supplied
     * currency.
     * Requires an exchange rate for the exchange direction to have been
     * registered, except when exchanging from one currency to the same
     * currency, in which case the supplied amount of money will always
     * be the result.
     * 
     * @param inMoneyToExchange Money to exchange to desired currency.
     * @param inToCurrency Currency to exchange to.
     * @return Exchanged money, or None if no exchange rate registered
     * for the requested exchange.
     */
    def exchange(inMoneyToExchange : Money, inToCurrency : Currency) :
    	Option[Money] = {
        /* Query-type method that do not alter state and returns a result. */
        
        val theCurrencyDirection = (inMoneyToExchange.currency, inToCurrency)
        
        theCurrencyDirection match {
            case (theFromCurrency, theToCurrency) if (theFromCurrency == theToCurrency) =>
                /* Special case: Exchange to same currency. */
                new Some(inMoneyToExchange)
            case _ =>
                /* Regular exchange from one currency to another. */
                performRegularTwoCurrencyExchange(inMoneyToExchange, inToCurrency)
        }
    }
    
    /**
     * Calculates the 
     */
    private def performRegularTwoCurrencyExchange(
        inMoneyToExchange : Money, inToCurrency : Currency) : Option[Money] = {
        val theCurrencyDirection = (inMoneyToExchange.currency, inToCurrency)
        val theExchangeRateOption = exchangeRateMap.get(theCurrencyDirection)
        
        theExchangeRateOption match {
            case Some(theExchangeRate : BigDecimal) =>
                /* Found exchange rate. Calculate the exchanged amount of money. */
                val theExchangedAmount = inMoneyToExchange.amount * theExchangeRate
                val theExchangedMoney = new Money(theExchangedAmount, inToCurrency)
                new Some(theExchangedMoney)
            case None =>
                /* No exchange rate for currency pair and direction found. */
                None
        }
    }
}