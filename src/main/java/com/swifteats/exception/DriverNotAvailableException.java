package com.swifteats.exception;

/**
 * Exception thrown when driver is not available for assignment
 */
public class DriverNotAvailableException extends SwiftEatsException {
    
    public DriverNotAvailableException(Long driverId) {
        super("Driver is not available: " + driverId, "DRIVER_NOT_AVAILABLE");
    }
    
    public DriverNotAvailableException(String message) {
        super(message, "DRIVER_NOT_AVAILABLE");
    }
}

