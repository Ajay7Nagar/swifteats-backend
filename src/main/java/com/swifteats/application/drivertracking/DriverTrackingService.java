package com.swifteats.application.drivertracking;

import com.swifteats.domain.drivertracking.DriverLocation;
import com.swifteats.domain.drivertracking.DriverTrackingRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public class DriverTrackingService {
    private static final Duration MIN_UPDATE_INTERVAL = Duration.ofSeconds(5);

    private final DriverTrackingRepository repository;

    public DriverTrackingService(DriverTrackingRepository repository) {
        this.repository = repository;
    }

    public void setStatus(UUID driverId, boolean online) {
        repository.setDriverStatus(driverId, online, OffsetDateTime.now());
    }

    public void ingestLocation(UUID driverId, UUID orderId, double lat, double lng, OffsetDateTime timestamp, OffsetDateTime lastSeenTimestamp) {
        validateCoordinates(lat, lng);
        if (!repository.isDriverOnline(driverId)) {
            throw new IllegalStateException("DRIVER_OFFLINE");
        }
        if (lastSeenTimestamp != null && Duration.between(lastSeenTimestamp, timestamp).compareTo(MIN_UPDATE_INTERVAL) < 0) {
            throw new IllegalArgumentException("RATE_LIMITED");
        }
        repository.upsertLatestLocation(driverId, orderId, lat, lng, timestamp);
    }

    public Optional<DriverLocation> getLatestForOrder(UUID orderId) {
        return repository.getLatestLocationForOrder(orderId);
    }

    private void validateCoordinates(double lat, double lng) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new IllegalArgumentException("INVALID_COORDINATES");
        }
    }
}


