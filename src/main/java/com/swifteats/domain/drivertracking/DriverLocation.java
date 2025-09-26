package com.swifteats.domain.drivertracking;

import java.time.OffsetDateTime;
import java.util.UUID;

public class DriverLocation {
    private UUID driverId;
    private UUID orderId;
    private double lat;
    private double lng;
    private OffsetDateTime timestamp;

    public DriverLocation(UUID driverId, UUID orderId, double lat, double lng, OffsetDateTime timestamp) {
        this.driverId = driverId;
        this.orderId = orderId;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    public UUID getDriverId() { return driverId; }
    public UUID getOrderId() { return orderId; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public OffsetDateTime getTimestamp() { return timestamp; }
}


