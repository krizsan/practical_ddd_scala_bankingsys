package com.ivan.bank.banking.services

import com.ivan.bank.domain.entities.BankAccount
import com.ivan.bank.domain.valueobjects.Money
import com.ivan.bank.domain.repositories.BankAccountRepository
import java.util.Currency
import com.ivan.bank.exchangerate.services.ExchangeRateService
import com.ivan.bank.domain.repositories.BankAccountRepository

/**
 * Provides services related to bank account banking.
 *
 * @author Ivan Krizsan
 */
class BankingService extends BankingServiceMixinInterface {
    /* Constant(s): */
    private val ACCOUNTNUMBER_FORMAT_REGEXP = """[0-9]{3}\.[0-9]{3}""".r

    /* Field(s): */
    var exchangeRateService : ExchangeRateService = null

    /**
     * Registers the supplied bank account with the service.
     * The account number in the bank account must not have been
     * previously used to register a bank account.
     *
     * @param inNewBankAccount Bank account to register with the service.
     */
    def registerBankAccount(inNewBankAccount : BankAccount) : Unit = {
        /*
         * This is a command-type method, so it does not return a result.
         * This method has a side effect, in that a bank account is
         * created in the repository.
         */
        validateBankAccountNumberFormat(inNewBankAccount.accountNumber)

        BankAccountRepository.create(inNewBankAccount)
    }

    /**
     * Inquires the balance of the bank account with the supplied
     * account number.
     *
     * @param inBankAccountNumber Account number of bank account for
     * which to inquire for balance.
     * @return Balance of the bank account.
     */
    def balance(inBankAccountNumber : String) : Money = {
        validateBankAccountNumberFormat(inBankAccountNumber)
        
        /*
         * This is a query-type method, so it does not have
         * any side-effects, it is idempotent.
         */
        
        val theBankAccountOption = retrieveBankAccount(inBankAccountNumber)
        val theBankAccount = theBankAccountOption.get

        theBankAccount.balance
    }
    
    /**
     * Deposits the supplied amount of money to the bank account with
     * the supplied account number.
     *
     * @param inBankAccountNumber Account number of bank account to
     * which to deposit money.
     * @param inAmount Amount of money to deposit to the account.
     */
    def deposit(inBankAccountNumber : String, inAmount : Money) : Unit = {
        /*
         * This is a command-type method, so we do not return a
         * result.
         * The method has side-effects in that the balance of a
         * bank account is updated.
         */
        /* Retrieve bank account with supplied account number. */
        val theBankAccountOption = retrieveBankAccount(inBankAccountNumber)
        val theBankAccount = theBankAccountOption.get

        /*
         * Exchange the currency to deposit to the currency of
         * the bank account. The exchange rate service will return
         * the supplied amount if it already is of the desired currency,
         * so it is safe to always perform the exchange operation.
         */
        val theExchangedAmountToDepositOption = exchangeMoney(
            inAmount, theBankAccount.currency)
        val theExchangedAmountToDeposit = theExchangedAmountToDepositOption.get

        /*
         * Arriving here, we know that we have a bank account,
         * money to deposit in the bank account's currency and can
         * now perform the deposit and update the bank account.
         */
        theBankAccount.deposit(theExchangedAmountToDeposit)
        updateBankAccount(theBankAccount)
    }
    
    /**
     * Withdraws the supplied amount of money from the bank account with
     * the supplied account number.
     *
     * @param inBankAccountNumber Account number of bank account from
     * which to withdraw money.
     * @param inAmount Amount of money to withdraw from the account.
     */
    def withdraw(inBankAccountNumber : String, inAmount : Money) : Unit = {
        /*
         * This is a command-type method, so we do not return a
         * result.
         * The method has side-effects in that the balance of a
         * bank account is updated.
         */
        /* Retrieve bank account with supplied account number. */
        val theBankAccountOption = retrieveBankAccount(inBankAccountNumber)
        val theBankAccount = theBankAccountOption.get

        /*
         * Exchange the currency to withdraw to the currency of
         * the bank account. The exchange rate service will do nothing if
         * the supplied amount is of the desired currency, so it is
         * safe to always perform the exchange operation.
         */
        val theExchangedAmountToWithdrawOption = exchangeMoney(inAmount,
            theBankAccount.currency)
        val theExchangedAmountToWithdraw = theExchangedAmountToWithdrawOption.get

        /*
         * Arriving here, we know that we have a bank account,
         * money to withdraw in the bank account's currency and can
         * now perform the withdrawal and update the bank account.
         */
        theBankAccount.withdraw(theExchangedAmountToWithdraw)
        updateBankAccount(theBankAccount)
    }
    
    /**
     * Validates the format of the account number of the supplied
     * bank account. If it is not in the appropriate format, throw
     * an exception.
     */
    protected def validateBankAccountNumberFormat(
        inBankAccountNumber : String) : Unit = {
        /*
       * Make sure that the account number is the proper format.
       * If the format is invalid, throws an exception.
       */
        inBankAccountNumber match {
            case ACCOUNTNUMBER_FORMAT_REGEXP() =>
            /* Good account number, do nothing. */
            case _ =>
                /* Bad account number, throw exception. */
                throw new IllegalArgumentException(
                    "Failed to register new bank account. " +
                        "Illegal account number format: " +
                        inBankAccountNumber)
        }
    }
    
    /**
     * Retrieves bank account with supplied account number from
     * the bank account repository.
     * This method isolates access to the bank account repository in
     * order to enable us to add error handling, exception translation,
     * logging etc of access to a repository.
     * Note that we assume only a scenario in which access to the
     * repository is successful.
     * 
     * @param inBankAccountNumber Account number of bank account
     * to retrieve.
     * @return Option holding bank account with supplied account number,
     * or None if no bank account was found.
     */
    protected def retrieveBankAccount(inBankAccountNumber : String) :
    	Option[BankAccount] = {
        val theBankAccountOption =
            BankAccountRepository.findBankAccountWithAccountNumber(
                inBankAccountNumber)
        theBankAccountOption
    }
    
    /**
     * Updates supplied bank account in the bank account repository.
     * This method isolates access to the bank account repository in
     * order to enable us to add error handling, exception translation,
     * logging etc of access to a repository.
     * Note that we assume only a scenario in which a bank account is
     * found.
     * 
     * @param inBankAccount Bank account to update.
     */
    protected def updateBankAccount(inBankAccount : BankAccount) :
    	Unit = {
        BankAccountRepository.update(inBankAccount)
    }
    
    /**
     * Exchanges the supplied amount of money to the supplied currency.
     * This method isolates access to the exchange rate service in
     * order to enable us to add error handling, exception translation,
     * logging etc of access to a particular service external to this service.
     * Note that we assume only a scenario in the exchange is successful.
     * 
     * @param inAmount Money to exchange.
     * @param inToCurrency Currency to exchange money to.
     * @return Option holding exchanged money, or None if no exchange
     * rate registered for the exchange.
     */
    protected def exchangeMoney(inAmount : Money, inToCurrency : Currency) :
    	Option[Money] = {
        val theExchangedMoneyOption = exchangeRateService.exchange(
            inAmount, inToCurrency)
        theExchangedMoneyOption
    }
}