package com.swifteats.exception;

/**
 * Exception thrown when invalid order state transition is attempted
 */
public class InvalidOrderStateException extends SwiftEatsException {
    
    public InvalidOrderStateException(String message) {
        super(message, "INVALID_ORDER_STATE");
    }
    
    public InvalidOrderStateException(String currentState, String newState) {
        super("Invalid order state transition from " + currentState + " to " + newState, 
              "INVALID_ORDER_STATE");
    }
}

