package com.swifteats.service;

import com.swifteats.dto.DriverLocationUpdateDto;
import com.swifteats.model.Driver;
import com.swifteats.model.DriverLocation;
import com.swifteats.repository.DriverLocationRepository;
import com.swifteats.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Driver management and real-time location tracking
 * Optimized for high-frequency GPS updates (2000 events/second)
 */
@Service
@Transactional
public class DriverService {
    
    private final DriverRepository driverRepository;
    private final DriverLocationRepository driverLocationRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Value("${swifteats.driver.retention-period}")
    private long locationRetentionPeriod;
    
    @Autowired
    public DriverService(DriverRepository driverRepository,
                        DriverLocationRepository driverLocationRepository,
                        KafkaTemplate<String, String> kafkaTemplate,
                        SimpMessagingTemplate messagingTemplate) {
        this.driverRepository = driverRepository;
        this.driverLocationRepository = driverLocationRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Update driver location - optimized for high throughput
     * Handles up to 2000 events/second from 10000 concurrent drivers
     */
    @Async
    public void updateDriverLocation(DriverLocationUpdateDto locationUpdate) {
        try {
            // Validate driver exists and is active
            Optional<Driver> driverOpt = driverRepository.findById(locationUpdate.getDriverId());
            if (driverOpt.isEmpty()) {
                throw new RuntimeException("Driver not found: " + locationUpdate.getDriverId());
            }
            
            Driver driver = driverOpt.get();
            if (driver.getStatus() == Driver.DriverStatus.OFFLINE) {
                // Don't process location updates for offline drivers
                return;
            }
            
            // Create location record
            DriverLocation location = new DriverLocation(
                locationUpdate.getDriverId(),
                locationUpdate.getLatitude(),
                locationUpdate.getLongitude(),
                locationUpdate.getTimestamp() != null ? locationUpdate.getTimestamp() : LocalDateTime.now()
            );
            location.setAccuracy(locationUpdate.getAccuracy());
            location.setSpeed(locationUpdate.getSpeed());
            location.setHeading(locationUpdate.getHeading());
            
            // Save location (async for performance)
            driverLocationRepository.save(location);
            
            // Update driver's current location in driver table
            driver.setCurrentLatitude(locationUpdate.getLatitude());
            driver.setCurrentLongitude(locationUpdate.getLongitude());
            driver.setLastLocationUpdate(location.getTimestamp());
            driverRepository.save(driver);
            
            // Publish location update to Kafka for real-time processing
            String locationJson = String.format(
                "{\"driverId\":%d,\"latitude\":%f,\"longitude\":%f,\"timestamp\":\"%s\",\"speed\":%f}",
                location.getDriverId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getTimestamp().toString(),
                location.getSpeed() != null ? location.getSpeed() : 0.0
            );
            
            kafkaTemplate.send("driver-locations", locationUpdate.getDriverId().toString(), locationJson);
            
            // Send real-time update via WebSocket for live tracking
            messagingTemplate.convertAndSend("/topic/driver-location/" + locationUpdate.getDriverId(), locationJson);
            
        } catch (Exception e) {
            System.err.println("Error updating driver location: " + e.getMessage());
            // Don't throw exception to avoid impacting throughput
        }
    }
    
    /**
     * Get driver's current location
     */
    @Transactional(readOnly = true)
    public Optional<DriverLocation> getDriverCurrentLocation(Long driverId) {
        return driverLocationRepository.findTopByDriverIdOrderByTimestampDesc(driverId);
    }
    
    /**
     * Get driver's location history within time range
     */
    @Transactional(readOnly = true)
    public List<DriverLocation> getDriverLocationHistory(Long driverId, LocalDateTime startTime, LocalDateTime endTime) {
        return driverLocationRepository.findByDriverIdAndTimestampBetweenOrderByTimestampDesc(
            driverId, startTime, endTime);
    }
    
    /**
     * Get recent locations for multiple drivers (for live tracking dashboard)
     */
    @Transactional(readOnly = true)
    public List<DriverLocation> getRecentLocationsForDrivers(List<Long> driverIds, LocalDateTime since) {
        return driverLocationRepository.findRecentLocationsByDriverIds(driverIds, since);
    }
    
    /**
     * Update driver status
     */
    public Driver updateDriverStatus(Long driverId, Driver.DriverStatus newStatus) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        
        driver.setStatus(newStatus);
        driver = driverRepository.save(driver);
        
        // Publish status update event
        kafkaTemplate.send("driver-events", "status-updated",
                String.format("{\"driverId\":%d,\"status\":\"%s\"}", driverId, newStatus));
        
        return driver;
    }
    
    /**
     * Get available drivers within radius
     */
    @Transactional(readOnly = true)
    public List<Driver> getAvailableDriversNearby(Double latitude, Double longitude, Double radiusKm) {
        return driverRepository.findAvailableDriversWithinRadius(latitude, longitude, radiusKm);
    }
    
    /**
     * Get all online drivers
     */
    @Transactional(readOnly = true)
    public List<Driver> getOnlineDrivers() {
        return driverRepository.findByStatus(Driver.DriverStatus.ONLINE);
    }
    
    /**
     * Get driver by ID
     */
    @Transactional(readOnly = true)
    public Driver getDriverById(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
    }
    
    /**
     * Create new driver
     */
    public Driver createDriver(Driver driver) {
        // Validate unique email and phone
        if (driverRepository.existsByEmail(driver.getEmail())) {
            throw new RuntimeException("Driver with email already exists");
        }
        if (driverRepository.existsByPhone(driver.getPhone())) {
            throw new RuntimeException("Driver with phone already exists");
        }
        
        return driverRepository.save(driver);
    }
    
    /**
     * Clean up old location data to maintain performance
     * Should be called periodically (e.g., via scheduled task)
     */
    @Async
    public void cleanupOldLocationData() {
        LocalDateTime cutoffTime = LocalDateTime.now().minus(Duration.ofMillis(locationRetentionPeriod));
        driverLocationRepository.deleteOldLocations(cutoffTime);
        System.out.println("Cleaned up location data older than: " + cutoffTime);
    }
    
    /**
     * Get location update statistics for monitoring
     */
    @Transactional(readOnly = true)
    public long getLocationUpdateCount(LocalDateTime startTime, LocalDateTime endTime) {
        return driverLocationRepository.countByTimestampBetween(startTime, endTime);
    }
}
