package com.swifteats.repository;

import com.swifteats.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Driver entity operations
 * Optimized for real-time driver tracking and assignment
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    /**
     * Find driver by email
     */
    Optional<Driver> findByEmail(String email);
    
    /**
     * Find drivers by status
     */
    List<Driver> findByStatus(Driver.DriverStatus status);
    
    /**
     * Find online drivers for assignment
     */
    List<Driver> findByStatusIn(List<Driver.DriverStatus> statuses);
    
    /**
     * Find available drivers within a radius for order assignment
     */
    @Query(value = "SELECT d.* FROM drivers d WHERE " +
           "d.status = 'ONLINE' AND " +
           "d.current_latitude IS NOT NULL AND d.current_longitude IS NOT NULL AND " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(d.current_latitude)) * " +
           "cos(radians(d.current_longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(d.current_latitude)))) < :radiusKm " +
           "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(d.current_latitude)) * " +
           "cos(radians(d.current_longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(d.current_latitude))))",
           nativeQuery = true)
    List<Driver> findAvailableDriversWithinRadius(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusKm") Double radiusKm
    );
    
    /**
     * Check if driver exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if driver exists by phone
     */
    boolean existsByPhone(String phone);
    
    /**
     * Count active drivers for monitoring
     */
    long countByStatus(Driver.DriverStatus status);
}
