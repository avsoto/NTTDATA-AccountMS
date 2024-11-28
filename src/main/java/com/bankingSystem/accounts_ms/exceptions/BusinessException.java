package com.bankingSystem.accounts_ms.exceptions;

/**
 * Custom exception class for handling business-related exceptions.
 * <p>
 * This exception is thrown when a business rule is violated or an error occurs
 * in the business logic of the banking system.
 * </p>
 */
public class BusinessException extends RuntimeException{

    /**
     * Constructs a new BusinessException with the specified detail message.
     *
     * @param message the detail message to describe the exception.
     */
    public BusinessException(String message) {
        super(message);
    }

}
