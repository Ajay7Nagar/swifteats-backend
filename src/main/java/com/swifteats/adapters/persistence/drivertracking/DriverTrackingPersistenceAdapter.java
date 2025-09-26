package com.swifteats.adapters.persistence.drivertracking;

import com.swifteats.domain.drivertracking.DriverLocation;
import com.swifteats.domain.drivertracking.DriverTrackingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DriverTrackingPersistenceAdapter implements DriverTrackingRepository {

    private final DriverStatusRepo statusRepo;
    private final DriverLocationLatestRepo latestRepo;

    public DriverTrackingPersistenceAdapter(DriverStatusRepo statusRepo, DriverLocationLatestRepo latestRepo) {
        this.statusRepo = statusRepo;
        this.latestRepo = latestRepo;
    }

    @Override
    @Transactional
    public void setDriverStatus(UUID driverId, boolean online, OffsetDateTime updatedAt) {
        DriverStatusEntity e = statusRepo.findById(driverId).orElseGet(() -> {
            DriverStatusEntity n = new DriverStatusEntity();
            n.driverId = driverId;
            return n;
        });
        e.online = online;
        e.updatedAt = updatedAt;
        statusRepo.save(e);
    }

    @Override
    public boolean isDriverOnline(UUID driverId) {
        return statusRepo.findById(driverId).map(d -> d.online).orElse(false);
    }

    @Override
    @Transactional
    public void upsertLatestLocation(UUID driverId, UUID orderId, double lat, double lng, OffsetDateTime timestamp) {
        DriverLocationLatestEntity e = latestRepo.findById(driverId).orElseGet(() -> {
            DriverLocationLatestEntity n = new DriverLocationLatestEntity();
            n.driverId = driverId;
            return n;
        });
        e.orderId = orderId;
        e.lat = lat;
        e.lng = lng;
        e.timestamp = timestamp;
        latestRepo.save(e);
    }

    @Override
    public Optional<DriverLocation> getLatestLocationForOrder(UUID orderId) {
        return latestRepo.findByOrderId(orderId)
                .map(e -> new DriverLocation(e.driverId, e.orderId, e.lat, e.lng, e.timestamp));
    }
}


