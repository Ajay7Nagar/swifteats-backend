package com.swifteats.repository;

import com.swifteats.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Restaurant entity operations
 * Optimized for high-performance menu & restaurant browsing (<200ms P99)
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    /**
     * Find restaurants by status - cached for performance
     */
    List<Restaurant> findByStatus(Restaurant.RestaurantStatus status);
    
    /**
     * Find restaurants by cuisine type - cached for performance
     */
    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);
    
    /**
     * Find restaurants within a certain radius using Haversine formula
     * Optimized for location-based queries
     */
    @Query(value = "SELECT r.* FROM restaurants r WHERE " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(r.latitude)) * " +
           "cos(radians(r.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(r.latitude)))) < :radiusKm " +
           "AND r.status = 'ACTIVE' " +
           "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(r.latitude)) * " +
           "cos(radians(r.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(r.latitude))))",
           nativeQuery = true)
    List<Restaurant> findRestaurantsWithinRadius(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusKm") Double radiusKm
    );
    
    /**
     * Find top-rated restaurants - cached for performance
     */
    @Query("SELECT r FROM Restaurant r WHERE r.status = 'ACTIVE' AND r.rating >= :minRating ORDER BY r.rating DESC")
    List<Restaurant> findTopRatedRestaurants(@Param("minRating") Double minRating);
    
    /**
     * Search restaurants by name or cuisine type
     */
    @Query("SELECT r FROM Restaurant r WHERE r.status = 'ACTIVE' AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Restaurant> searchRestaurants(@Param("searchTerm") String searchTerm);
}
