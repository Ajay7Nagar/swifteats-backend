package com.swifteats.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DriverLocation entity for tracking real-time GPS location data
 * Optimized for high-frequency updates (2000 events/second)
 */
@Entity
@Table(name = "driver_locations", indexes = {
    @Index(name = "idx_driver_location_driver", columnList = "driver_id"),
    @Index(name = "idx_driver_location_timestamp", columnList = "timestamp"),
    @Index(name = "idx_driver_location_driver_timestamp", columnList = "driver_id, timestamp")
})
public class DriverLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "driver_id", nullable = false)
    private Long driverId;
    
    @NotNull
    @Column(nullable = false)
    private Double latitude;
    
    @NotNull
    @Column(nullable = false)
    private Double longitude;
    
    @Column(name = "accuracy")
    private Double accuracy; // GPS accuracy in meters
    
    @Column(name = "speed")
    private Double speed; // Speed in km/h
    
    @Column(name = "heading")
    private Double heading; // Direction in degrees
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    // Constructors
    public DriverLocation() {}
    
    public DriverLocation(Long driverId, Double latitude, Double longitude) {
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = LocalDateTime.now();
    }
    
    public DriverLocation(Long driverId, Double latitude, Double longitude, LocalDateTime timestamp) {
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
    
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    
    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
