package com.sunrise.dental.exception;

/**
 * Thrown when a business rule is violated (double booking, invalid state
 * transition, out-of-hours booking, etc.).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
