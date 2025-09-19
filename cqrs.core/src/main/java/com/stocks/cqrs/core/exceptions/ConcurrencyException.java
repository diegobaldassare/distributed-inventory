package com.distributedinventory.cqrs.core.exceptions;

public class ConcurrencyException extends RuntimeException {
    
    public ConcurrencyException() {
        super();
    }
    
    public ConcurrencyException(String message) {
        super(message);
    }
    
    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
