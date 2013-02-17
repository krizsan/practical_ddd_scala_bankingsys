package com.ivan.bank.banking.exceptiontranslation.exceptions

/**
 * Exception that indicates than an attempt was made to retrieve an
 * exchange rate that has not been registered with the system.
 * 
 * @author Ivan Krizsan
 */
class NoExchangeRateRegistered(message : String, cause : Throwable) extends
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