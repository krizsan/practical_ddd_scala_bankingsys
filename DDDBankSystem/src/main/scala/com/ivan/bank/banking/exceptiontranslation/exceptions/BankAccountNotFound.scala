package com.ivan.bank.banking.exceptiontranslation.exceptions

/**
 * Exception that indicates that an attempt was made to perform an
 * operation on a bank account that does not exist.
 * 
 * @author Ivan Krizsan
 */
class BankAccountNotFound(message : String, cause : Throwable) extends
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