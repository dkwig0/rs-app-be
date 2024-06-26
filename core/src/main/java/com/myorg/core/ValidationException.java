package com.myorg.core;

/**
 * @author Aliaksei Tsvirko
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
