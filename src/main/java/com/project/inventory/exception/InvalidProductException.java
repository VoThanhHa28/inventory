package com.project.inventory.exception;

/**
 * Exception thrown when product is not found
 */
public class InvalidProductException extends RuntimeException {
    
    public InvalidProductException(String message) {
        super(message);
    }

    public InvalidProductException(String message, Throwable cause) {
        super(message, cause);
    }
}
