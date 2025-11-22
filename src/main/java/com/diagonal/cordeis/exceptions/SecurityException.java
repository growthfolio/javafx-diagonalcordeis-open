package com.diagonal.cordeis.exceptions;

/**
 * Exception thrown for security-related issues like invalid credentials,
 * account lockouts, etc.
 */
public class SecurityException extends RuntimeException {

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
