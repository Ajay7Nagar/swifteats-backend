package com.swifteats.exception;

/**
 * Exception thrown when a customer is not found
 */
public class CustomerNotFoundException extends SwiftEatsException {
    
    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with id: " + customerId, "CUSTOMER_NOT_FOUND");
    }
    
    public CustomerNotFoundException(String message) {
        super(message, "CUSTOMER_NOT_FOUND");
    }
}

