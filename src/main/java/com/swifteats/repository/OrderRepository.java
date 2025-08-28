package com.swifteats.repository;

import com.swifteats.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Order entity operations
 * Optimized for high-volume order processing (500 orders/min)
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find orders by customer
     */
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    
    /**
     * Find orders by restaurant
     */
    Page<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);
    
    /**
     * Find orders by driver
     */
    List<Order> findByDriverId(Long driverId);
    
    /**
     * Find orders by status
     */
    List<Order> findByStatus(Order.OrderStatus status);
    
    /**
     * Find orders by status and restaurant for restaurant dashboard
     */
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, Order.OrderStatus status);
    
    /**
     * Find active orders for a driver (not delivered or cancelled)
     */
    @Query("SELECT o FROM Order o WHERE o.driver.id = :driverId AND " +
           "o.status NOT IN ('DELIVERED', 'CANCELLED')")
    List<Order> findActiveOrdersByDriverId(@Param("driverId") Long driverId);
    
    /**
     * Find orders within a time range for analytics
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startTime AND :endTime")
    List<Order> findOrdersWithinTimeRange(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * Count orders by status for dashboard metrics
     */
    long countByStatus(Order.OrderStatus status);
    
    /**
     * Count orders created within last hour for rate limiting
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt > :oneHourAgo")
    long countOrdersInLastHour(@Param("oneHourAgo") LocalDateTime oneHourAgo);
    
    /**
     * Find pending orders waiting for driver assignment
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'READY_FOR_PICKUP' AND o.driver IS NULL " +
           "ORDER BY o.createdAt ASC")
    List<Order> findPendingOrdersForAssignment();
}
