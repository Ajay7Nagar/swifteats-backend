package com.swifteats.exception;

/**
 * Base exception class for SwiftEats application
 */
public abstract class SwiftEatsException extends RuntimeException {
    
    private final String errorCode;
    
    public SwiftEatsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SwiftEatsException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

