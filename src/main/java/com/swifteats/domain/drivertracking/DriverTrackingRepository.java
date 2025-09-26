package com.swifteats.domain.drivertracking;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface DriverTrackingRepository {
    void setDriverStatus(UUID driverId, boolean online, OffsetDateTime updatedAt);
    boolean isDriverOnline(UUID driverId);
    void upsertLatestLocation(UUID driverId, UUID orderId, double lat, double lng, OffsetDateTime timestamp);
    Optional<DriverLocation> getLatestLocationForOrder(UUID orderId);
}


