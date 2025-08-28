package com.swifteats.repository;

import com.swifteats.model.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DriverLocation entity operations
 * Optimized for high-frequency GPS location updates (2000 events/second)
 */
@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
    
    /**
     * Find latest location for a driver
     */
    Optional<DriverLocation> findTopByDriverIdOrderByTimestampDesc(Long driverId);
    
    /**
     * Find location history for a driver within time range
     */
    List<DriverLocation> findByDriverIdAndTimestampBetweenOrderByTimestampDesc(
        Long driverId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find recent locations for multiple drivers (for live tracking)
     */
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driverId IN :driverIds AND " +
           "dl.timestamp > :since ORDER BY dl.driverId, dl.timestamp DESC")
    List<DriverLocation> findRecentLocationsByDriverIds(
        @Param("driverIds") List<Long> driverIds, 
        @Param("since") LocalDateTime since);
    
    /**
     * Get latest location for each driver since a specific time
     */
    @Query(value = "SELECT DISTINCT ON (driver_id) * FROM driver_locations " +
           "WHERE timestamp > :since ORDER BY driver_id, timestamp DESC", 
           nativeQuery = true)
    List<DriverLocation> findLatestLocationPerDriverSince(@Param("since") LocalDateTime since);
    
    /**
     * Clean up old location data for performance
     * Remove locations older than retention period
     */
    @Modifying
    @Query("DELETE FROM DriverLocation dl WHERE dl.timestamp < :cutoffTime")
    void deleteOldLocations(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Count location updates for monitoring
     */
    long countByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
}
