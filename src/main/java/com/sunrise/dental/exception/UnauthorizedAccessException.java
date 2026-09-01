package com.sunrise.dental.exception;

/**
 * Thrown when the current user is not authorised to perform an action.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
