package com.swifteats.exception;

/**
 * Exception thrown when an order is not found
 */
public class OrderNotFoundException extends SwiftEatsException {
    
    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId, "ORDER_NOT_FOUND");
    }
    
    public OrderNotFoundException(String message) {
        super(message, "ORDER_NOT_FOUND");
    }
}

