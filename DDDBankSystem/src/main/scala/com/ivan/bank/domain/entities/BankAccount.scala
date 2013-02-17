package com.ivan.bank.domain.entities

import java.util.Currency
import scala.math.BigDecimal.double2bigDecimal
import com.ivan.bank.domain.valueobjects.Money

/**
 * Represents an account in a bank.
 * 
 * @author Ivan Krizsan
 */
class BankAccount(val currency : Currency) extends Cloneable {
    /* Constructor code: */
    require(currency != null)
    
    /* Constant(s): */
    
    /* Field(s): */
    var balance : Money = new Money(0.0, currency)
    var accountNumber : String = null
    
    
    /**
     * Withdraws supplied amount from the account.
     * 
     * @param inAmount Amount to withdraw. Must be greater than, or
     * equal to, zero.
     */
    def withdraw(inAmount : Money) : Unit = {
        require(inAmount.amount >= 0.0, "must withdraw positive amounts")
        require(inAmount.currency == currency, "must withdraw same currency")
        assume(balance.amount - inAmount.amount >= 0.0, "overdrafts not allowed")
        
        balance = balance.subtract(inAmount)
    }
    
    /**
     * Deposits supplied amount to the account.
     * 
     * @param inAmount Amount to deposit. Must be greater than, or
     * equal to, zero.
     */
    def deposit(inAmount : Money) : Unit = {
        require(inAmount.amount >= 0.0, "must deposit positive amounts")
        require(inAmount.currency == currency, "must deposit same currency")
        
        balance = balance.add(inAmount)
    }
    
    /**
     * Clones this bank account by performing a deep copy of it.
     * 
     * @return Clone of this bank account.
     */
    override def clone() : BankAccount = {
        val theClone = new BankAccount(currency)
        theClone.accountNumber = accountNumber
        /*
         * The Value Object holding the balance of the account can be used
         * by both this account and the cloned copy, since it is immutable.
         */
        theClone.balance = balance
        theClone
    }
}