package com.ivan.bank.banking.exceptiontranslation.exceptions

/**
 * Exception that indicates than an attempt to overdraft a bank
 * account was made.
 * 
 * @author Ivan Krizsan
 */
class BankAccountOverdraft(message : String, cause : Throwable) extends
    Exception(message, cause) {
    
    /**
     * Auxiliary constructor creating an exception without message or cause.
     */
    def this() = {
        this(null, null)
    }
    
    /**
     * Auxiliary constructor that creates an exception with a message.
     * 
     * @param inMessage Message string to be contained in exception.
     */
    def this(inMessage : String) = {
        this(inMessage, null)
    }
}