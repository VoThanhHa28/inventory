package com.project.inventory.exception;

/**
 * Exception thrown when quantity is negative or invalid
 */
public class NegativeQuantityException extends RuntimeException {
    
    public NegativeQuantityException(String message) {
        super(message);
    }

    public NegativeQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}
