package com.swifteats.repository;

import com.swifteats.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for MenuItem entity operations
 * Optimized for high-performance menu browsing (<200ms P99)
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    /**
     * Find available menu items by restaurant - heavily cached for performance
     */
    List<MenuItem> findByRestaurantIdAndAvailable(Long restaurantId, Boolean available);
    
    /**
     * Find all menu items by restaurant (including unavailable)
     */
    List<MenuItem> findByRestaurantIdOrderByCategory(Long restaurantId);
    
    /**
     * Find menu items by category for a restaurant - cached for performance
     */
    List<MenuItem> findByRestaurantIdAndCategoryAndAvailable(
        Long restaurantId, String category, Boolean available);
    
    /**
     * Search menu items by name within a restaurant
     */
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND " +
           "m.available = true AND LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<MenuItem> searchMenuItems(@Param("restaurantId") Long restaurantId, 
                                  @Param("searchTerm") String searchTerm);
    
    /**
     * Find vegetarian menu items for a restaurant
     */
    List<MenuItem> findByRestaurantIdAndIsVegetarianAndAvailable(
        Long restaurantId, Boolean isVegetarian, Boolean available);
    
    /**
     * Find vegan menu items for a restaurant
     */
    List<MenuItem> findByRestaurantIdAndIsVeganAndAvailable(
        Long restaurantId, Boolean isVegan, Boolean available);
    
    /**
     * Get distinct categories for a restaurant - cached for performance
     */
    @Query("SELECT DISTINCT m.category FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.available = true")
    List<String> findDistinctCategoriesByRestaurantId(@Param("restaurantId") Long restaurantId);
}
