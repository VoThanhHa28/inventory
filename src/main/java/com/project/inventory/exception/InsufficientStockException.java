package com.project.inventory.exception;

/**
 * Exception thrown when inventory stock is insufficient for the requested operation
 */
public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
