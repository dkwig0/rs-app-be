package com.myorg.core.exception;

/**
 * @author Aliaksei Tsvirko
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
