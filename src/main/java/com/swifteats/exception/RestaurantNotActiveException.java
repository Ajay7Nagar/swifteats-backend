package com.swifteats.exception;

/**
 * Exception thrown when trying to operate on inactive restaurant
 */
public class RestaurantNotActiveException extends SwiftEatsException {
    
    public RestaurantNotActiveException(Long restaurantId) {
        super("Restaurant is not active: " + restaurantId, "RESTAURANT_NOT_ACTIVE");
    }
    
    public RestaurantNotActiveException(String message) {
        super(message, "RESTAURANT_NOT_ACTIVE");
    }
}

