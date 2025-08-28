package com.swifteats.exception;

/**
 * Exception thrown when API rate limit is exceeded
 */
public class RateLimitExceededException extends SwiftEatsException {
    
    public RateLimitExceededException() {
        super("Order rate limit exceeded. Please try again later.", "RATE_LIMIT_EXCEEDED");
    }
    
    public RateLimitExceededException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED");
    }
}

