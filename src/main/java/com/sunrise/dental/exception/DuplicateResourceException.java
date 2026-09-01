package com.sunrise.dental.exception;

/**
 * Thrown when a uniqueness constraint is violated (e.g. duplicate reg number).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
