package com.swifteats.exception;

/**
 * Exception thrown when payment processing fails
 */
public class PaymentProcessingException extends SwiftEatsException {
    
    public PaymentProcessingException(String message) {
        super(message, "PAYMENT_PROCESSING_FAILED");
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, "PAYMENT_PROCESSING_FAILED", cause);
    }
}

