package com.swifteats.exception;

/**
 * Exception thrown when a driver is not found
 */
public class DriverNotFoundException extends SwiftEatsException {
    
    public DriverNotFoundException(Long driverId) {
        super("Driver not found with id: " + driverId, "DRIVER_NOT_FOUND");
    }
    
    public DriverNotFoundException(String message) {
        super(message, "DRIVER_NOT_FOUND");
    }
}

