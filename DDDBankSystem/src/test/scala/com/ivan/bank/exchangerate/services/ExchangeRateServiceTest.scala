package com.ivan.bank.exchangerate.services

import java.util.Currency
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import com.ivan.bank.domain.valueobjects.Money
import scala.math.BigDecimal.double2bigDecimal
import org.scalatest.junit.JUnitRunner

/**
 * Tests the <code>ExchangeRateService</code> class.
 *
 * @author Ivan Krizsan
 */
@RunWith(classOf[JUnitRunner])
class ExchangeRateServiceTest extends FunSuite with BeforeAndAfterEach {
    /* Constant(s): */
    private val CURRENCY_TWD = Currency.getInstance("TWD")
    private val CURRENCY_SEK = Currency.getInstance("SEK")

    private val EXCHANGERATE_SEK_TWD : BigDecimal = 4.0
    private val EXCHANGERATE_TWD_SEK : BigDecimal = 0.2

    private val MONEY_10_SEK = new Money(10.0, CURRENCY_SEK)
    private val MONEY_40_TWD = new Money(40.0, CURRENCY_TWD)

    /* Field(s): */
    private var exchangeRateService : ExchangeRateService = null

    override def beforeEach() {
        /* Creates a new exchange rate service before each test. */
        exchangeRateService = new ExchangeRateService()
    }

    test("It should be possible to register an exchange rate from " +
    	 "one currency to another currency") {
        /* Register exchange rate from TWD to SEK. */
        exchangeRateService.registerExchangeRate(CURRENCY_TWD, CURRENCY_SEK,
            EXCHANGERATE_TWD_SEK)
    }

    test("It should be possible to register an exchange rate from " +
    	 "one currency to another currency and vice versa") {
        /* Register exchange rates from TWD to SEK and vice versa. */
        exchangeRateService.registerExchangeRate(CURRENCY_TWD, CURRENCY_SEK,
            EXCHANGERATE_TWD_SEK)
        exchangeRateService.registerExchangeRate(CURRENCY_SEK, CURRENCY_TWD,
            EXCHANGERATE_SEK_TWD)
    }

    test("If several exchange rates from currency A to " +
    	 "currency B are registered, the last registered exchange rate " +
    	 "should be used when calculating an exchange") {
        /* Register exchange rate from TWD to SEK. */
        exchangeRateService.registerExchangeRate(CURRENCY_TWD, CURRENCY_SEK,
            0.2)

        /* Exchange from TWD to SEK. */
        var theExchangedSekMoneyOption =
            exchangeRateService.exchange(MONEY_40_TWD, CURRENCY_SEK)
        assert(theExchangedSekMoneyOption.isDefined)
        var theExchangedSekMoney = theExchangedSekMoneyOption.get
        assert(theExchangedSekMoney.amount == 8.0)
        assert(theExchangedSekMoney.currency == CURRENCY_SEK)

        /* Register new exchange rate from TWD to SEK. */
        exchangeRateService.registerExchangeRate(CURRENCY_TWD, CURRENCY_SEK,
            0.1)

        /* Exchange from TWD to SEK. The second exchange rate should be in effect. */
        theExchangedSekMoneyOption =
            exchangeRateService.exchange(MONEY_40_TWD, CURRENCY_SEK)
        assert(theExchangedSekMoneyOption.isDefined)
        theExchangedSekMoney = theExchangedSekMoneyOption.get
        assert(theExchangedSekMoney.amount == 4.0)
        assert(theExchangedSekMoney.currency == CURRENCY_SEK)
    }

    test("If an exchange rate from currency A to currency B has been " +
    	 "registered but no exchange rate from currency B to currency A, " +
    	 "it should be possible to exchange money in one direction only") {
        /* Register exchange rate from TWD to SEK. */
        exchangeRateService.registerExchangeRate(CURRENCY_TWD, CURRENCY_SEK,
            EXCHANGERATE_TWD_SEK)

        /* Exchange from TWD to SEK. */
        var theExchangedSekMoneyOption =
            exchangeRateService.exchange(MONEY_40_TWD, CURRENCY_SEK)
        assert(theExchangedSekMoneyOption.isDefined)
        val theExchangedSekMoney = theExchangedSekMoneyOption.get
        assert(theExchangedSekMoney.amount == 8.0)
        assert(theExchangedSekMoney.currency == CURRENCY_SEK)

        /* Attempt exchange from SEK to TWD. Should fail. */
        theExchangedSekMoneyOption =
            exchangeRateService.exchange(MONEY_10_SEK, CURRENCY_TWD)
        assert(theExchangedSekMoneyOption.isEmpty)

    }

    test("If registering one exchange rate from currency A to B and one " +
    	 "from currency B to A which, when multiplied, must not be equal " +
    	 "to 1, and then exchanging money from currency A to B and vice " +
    	 "versa, the resulting amount in currency A should not be equal to " +
    	 "the original amount in currency A") {
        /* Register exchange rates from TWD to SEK and vice versa. */
        exchangeRateService.registerExchangeRate(CURRENCY_TWD, CURRENCY_SEK,
            EXCHANGERATE_TWD_SEK)
        exchangeRateService.registerExchangeRate(CURRENCY_SEK, CURRENCY_TWD,
            EXCHANGERATE_SEK_TWD)

        /* Exchange from TWD to SEK. */
        val theExchangedSekMoneyOption =
            exchangeRateService.exchange(MONEY_40_TWD, CURRENCY_SEK)
        assert(theExchangedSekMoneyOption.isDefined)
        val theExchangedSekMoney = theExchangedSekMoneyOption.get
        assert(theExchangedSekMoney.amount == 8.0)
        assert(theExchangedSekMoney.currency == CURRENCY_SEK)

        /*
         * Exchange from SEK to TWD.
         * Note that the bank buys cheap and sells expensive, so we
         * loose 8 TWD in the process.
         */
        val theExchangedTwdMoneyOption = exchangeRateService.exchange(
            theExchangedSekMoney, CURRENCY_TWD)
        assert(theExchangedTwdMoneyOption.isDefined)
        val theExchangedTwdMoney = theExchangedTwdMoneyOption.get
        assert(theExchangedTwdMoney.amount == 32.0)
        assert(theExchangedTwdMoney.currency == CURRENCY_TWD)
    }
    
    test("It should always be possible to calculate the exchange from " +
    	 "a currency A to the same currency A. " +
    	 "The result should be the original amount") {
        /* Exchange from SEK to SEK. */
        val theExchangedSekMoneyOption =
            exchangeRateService.exchange(MONEY_10_SEK, CURRENCY_SEK)
        assert(theExchangedSekMoneyOption.isDefined)
        val theExchangedSekMoney = theExchangedSekMoneyOption.get
        assert(theExchangedSekMoney.amount == 10.0)
        assert(theExchangedSekMoney.currency == CURRENCY_SEK)
    }
}