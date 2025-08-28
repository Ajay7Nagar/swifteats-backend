package com.swifteats.exception;

/**
 * Exception thrown when a restaurant is not found
 */
public class RestaurantNotFoundException extends SwiftEatsException {
    
    public RestaurantNotFoundException(Long restaurantId) {
        super("Restaurant not found with id: " + restaurantId, "RESTAURANT_NOT_FOUND");
    }
    
    public RestaurantNotFoundException(String message) {
        super(message, "RESTAURANT_NOT_FOUND");
    }
}

