package com.ivan.bank.banking.exceptiontranslation.exceptions

/**
 * Exception that indicates that an attempt was made to create a
 * bank account with an account number for which there already
 * exist a bank account.
 * 
 * @author Ivan Krizsan
 */
class BankAccountAlreadyExists(message : String, cause : Throwable) extends
    Exception(message, cause) {

    /*
     * We must declare all the parameters in the primary constructor
     * which consists of the class body since this is the only constructor
     * that is able to invoke a superclass constructor.
     * The auxiliary constructors, which must invoke another constructor
     * declared before it, may supply null values for parameters not present.
     */
    
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