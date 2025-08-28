package com.swifteats.controller;

import com.swifteats.dto.DriverLocationUpdateDto;
import com.swifteats.model.Driver;
import com.swifteats.model.DriverLocation;
import com.swifteats.service.DriverAssignmentService;
import com.swifteats.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Driver management and real-time location tracking
 * Optimized for high-frequency GPS updates (2000 events/second)
 */
@RestController
@RequestMapping("/drivers")
@Tag(name = "Driver Management", description = "APIs for driver management and real-time location tracking")
public class DriverController {
    
    private final DriverService driverService;
    private final DriverAssignmentService driverAssignmentService;
    
    @Autowired
    public DriverController(DriverService driverService, DriverAssignmentService driverAssignmentService) {
        this.driverService = driverService;
        this.driverAssignmentService = driverAssignmentService;
    }
    
    /**
     * Update driver location (high-frequency endpoint)
     */
    @PostMapping("/location")
    @Operation(summary = "Update driver location", description = "Update driver's GPS location for real-time tracking")
    public ResponseEntity<Void> updateDriverLocation(@Valid @RequestBody DriverLocationUpdateDto locationUpdate) {
        try {
            driverService.updateDriverLocation(locationUpdate);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Batch update driver locations (for efficiency)
     */
    @PostMapping("/location/batch")
    @Operation(summary = "Batch update driver locations", description = "Update multiple driver locations in a single request")
    public ResponseEntity<Void> updateDriverLocationsBatch(@Valid @RequestBody List<DriverLocationUpdateDto> locationUpdates) {
        try {
            for (DriverLocationUpdateDto update : locationUpdates) {
                driverService.updateDriverLocation(update);
            }
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get driver's current location
     */
    @GetMapping("/{driverId}/location")
    @Operation(summary = "Get driver current location", description = "Retrieve driver's most recent location")
    public ResponseEntity<DriverLocation> getDriverCurrentLocation(
            @Parameter(description = "Driver ID") @PathVariable Long driverId) {
        
        Optional<DriverLocation> location = driverService.getDriverCurrentLocation(driverId);
        return location.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get driver's location history
     */
    @GetMapping("/{driverId}/location/history")
    @Operation(summary = "Get driver location history", description = "Retrieve driver's location history within time range")
    public ResponseEntity<List<DriverLocation>> getDriverLocationHistory(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Parameter(description = "Start time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<DriverLocation> locations = driverService.getDriverLocationHistory(driverId, startTime, endTime);
        return ResponseEntity.ok(locations);
    }
    
    /**
     * Get driver by ID
     */
    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver by ID", description = "Retrieve driver details by ID")
    public ResponseEntity<Driver> getDriverById(
            @Parameter(description = "Driver ID") @PathVariable Long driverId) {
        
        try {
            Driver driver = driverService.getDriverById(driverId);
            return ResponseEntity.ok(driver);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update driver status
     */
    @PutMapping("/{driverId}/status")
    @Operation(summary = "Update driver status", description = "Update driver availability status")
    public ResponseEntity<Driver> updateDriverStatus(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Parameter(description = "New driver status") @RequestParam String status) {
        
        try {
            Driver.DriverStatus driverStatus = Driver.DriverStatus.valueOf(status.toUpperCase());
            Driver driver = driverService.updateDriverStatus(driverId, driverStatus);
            return ResponseEntity.ok(driver);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get online drivers
     */
    @GetMapping("/online")
    @Operation(summary = "Get online drivers", description = "Retrieve all currently online drivers")
    public ResponseEntity<List<Driver>> getOnlineDrivers() {
        List<Driver> drivers = driverService.getOnlineDrivers();
        return ResponseEntity.ok(drivers);
    }
    
    /**
     * Get available drivers near location
     */
    @GetMapping("/nearby")
    @Operation(summary = "Get nearby available drivers", description = "Find available drivers within specified radius")
    public ResponseEntity<List<Driver>> getNearbyDrivers(
            @Parameter(description = "Latitude coordinate") @RequestParam Double latitude,
            @Parameter(description = "Longitude coordinate") @RequestParam Double longitude,
            @Parameter(description = "Search radius in kilometers", example = "5.0") @RequestParam(defaultValue = "5.0") Double radius) {
        
        List<Driver> drivers = driverService.getAvailableDriversNearby(latitude, longitude, radius);
        return ResponseEntity.ok(drivers);
    }
    
    /**
     * Create new driver
     */
    @PostMapping
    @Operation(summary = "Create new driver", description = "Register a new driver in the system")
    public ResponseEntity<Driver> createDriver(@Valid @RequestBody Driver driver) {
        try {
            Driver createdDriver = driverService.createDriver(driver);
            return ResponseEntity.ok(createdDriver);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get recent locations for multiple drivers (for live tracking dashboard)
     */
    @GetMapping("/locations/recent")
    @Operation(summary = "Get recent locations for multiple drivers", description = "Retrieve recent locations for live tracking dashboard")
    public ResponseEntity<List<DriverLocation>> getRecentLocationsForDrivers(
            @Parameter(description = "Driver IDs") @RequestParam List<Long> driverIds,
            @Parameter(description = "Since time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        
        List<DriverLocation> locations = driverService.getRecentLocationsForDrivers(driverIds, since);
        return ResponseEntity.ok(locations);
    }
    
    /**
     * Manually assign driver to order
     */
    @PostMapping("/{driverId}/assign-order/{orderId}")
    @Operation(summary = "Assign driver to order", description = "Manually assign a specific driver to an order")
    public ResponseEntity<Void> assignDriverToOrder(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        
        boolean success = driverAssignmentService.assignSpecificDriverToOrder(orderId, driverId);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
    
    /**
     * Unassign driver from order
     */
    @DeleteMapping("/{driverId}/unassign-order/{orderId}")
    @Operation(summary = "Unassign driver from order", description = "Remove driver assignment from an order")
    public ResponseEntity<Void> unassignDriverFromOrder(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        
        boolean success = driverAssignmentService.unassignDriverFromOrder(orderId);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
    
    /**
     * Get location update statistics (for monitoring)
     */
    @GetMapping("/stats/location-updates")
    @Operation(summary = "Get location update statistics", description = "Retrieve statistics for monitoring location update performance")
    public ResponseEntity<Long> getLocationUpdateStats(
            @Parameter(description = "Start time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        long count = driverService.getLocationUpdateCount(startTime, endTime);
        return ResponseEntity.ok(count);
    }
}
