package com.swifteats.service;

import com.swifteats.model.Driver;
import com.swifteats.model.Order;
import com.swifteats.repository.DriverRepository;
import com.swifteats.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for intelligent driver assignment to orders
 * Implements efficient algorithms for optimal driver-order matching
 */
@Service
@Transactional
public class DriverAssignmentService {
    
    private final DriverRepository driverRepository;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    public DriverAssignmentService(DriverRepository driverRepository,
                                 OrderRepository orderRepository,
                                 KafkaTemplate<String, String> kafkaTemplate) {
        this.driverRepository = driverRepository;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Assign a driver to an order using intelligent matching
     */
    @Async
    public void assignDriverToOrder(Order order) {
        try {
            Optional<Driver> bestDriver = findBestDriverForOrder(order);
            
            if (bestDriver.isPresent()) {
                Driver driver = bestDriver.get();
                
                // Update order and driver
                order.setDriver(driver);
                order.setStatus(Order.OrderStatus.PICKED_UP);
                driver.setStatus(Driver.DriverStatus.ON_DELIVERY);
                
                orderRepository.save(order);
                driverRepository.save(driver);
                
                // Publish assignment event
                kafkaTemplate.send("driver-assignments", "assignment-created",
                        String.format("{\"orderId\":%d,\"driverId\":%d,\"restaurantLat\":%f,\"restaurantLng\":%f,\"deliveryLat\":%f,\"deliveryLng\":%f}",
                                order.getId(),
                                driver.getId(),
                                order.getRestaurant().getLatitude(),
                                order.getRestaurant().getLongitude(),
                                order.getDeliveryLatitude() != null ? order.getDeliveryLatitude() : 0.0,
                                order.getDeliveryLongitude() != null ? order.getDeliveryLongitude() : 0.0));
                
                System.out.println("Driver " + driver.getId() + " assigned to order " + order.getId());
            } else {
                System.out.println("No available driver found for order " + order.getId());
                // Could implement queue system or notifications here
            }
            
        } catch (Exception e) {
            System.err.println("Error assigning driver to order " + order.getId() + ": " + e.getMessage());
        }
    }
    
    /**
     * Find the best driver for an order using multiple criteria
     */
    private Optional<Driver> findBestDriverForOrder(Order order) {
        // Get restaurant location
        Double restaurantLat = order.getRestaurant().getLatitude();
        Double restaurantLng = order.getRestaurant().getLongitude();
        
        // Find available drivers within reasonable radius (10km)
        List<Driver> availableDrivers = driverRepository.findAvailableDriversWithinRadius(
                restaurantLat, restaurantLng, 10.0);
        
        if (availableDrivers.isEmpty()) {
            return Optional.empty();
        }
        
        // Find best driver using scoring algorithm
        Driver bestDriver = null;
        double bestScore = Double.MIN_VALUE;
        
        for (Driver driver : availableDrivers) {
            double score = calculateDriverScore(driver, order);
            if (score > bestScore) {
                bestScore = score;
                bestDriver = driver;
            }
        }
        
        return Optional.ofNullable(bestDriver);
    }
    
    /**
     * Calculate driver score for assignment priority
     * Higher score = better match
     */
    private double calculateDriverScore(Driver driver, Order order) {
        double score = 0.0;
        
        // Factor 1: Distance to restaurant (closer is better)
        double distanceToRestaurant = calculateDistance(
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude(),
                order.getRestaurant().getLatitude(),
                order.getRestaurant().getLongitude()
        );
        
        // Inverse distance score (max 100 points, min at 10km)
        double distanceScore = Math.max(0, 100 - (distanceToRestaurant * 10));
        score += distanceScore;
        
        // Factor 2: Driver rating (higher rating is better)
        if (driver.getRating() != null) {
            score += driver.getRating() * 20; // Max 100 points for 5-star rating
        }
        
        // Factor 3: Driver experience (more deliveries is better)
        if (driver.getTotalDeliveries() != null) {
            score += Math.min(driver.getTotalDeliveries() * 0.1, 50); // Max 50 points
        }
        
        // Factor 4: Time since last location update (recent is better)
        if (driver.getLastLocationUpdate() != null) {
            long minutesSinceUpdate = java.time.Duration.between(
                    driver.getLastLocationUpdate(),
                    java.time.LocalDateTime.now()
            ).toMinutes();
            
            // Penalize drivers with stale location data
            if (minutesSinceUpdate > 5) {
                score -= minutesSinceUpdate * 2;
            }
        }
        
        return score;
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return Double.MAX_VALUE; // Invalid coordinates
        }
        
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in km
    }
    
    /**
     * Scheduled task to assign drivers to pending orders
     * Runs every 30 seconds to check for unassigned orders
     */
    @Scheduled(fixedDelay = 30000) // 30 seconds
    public void assignDriversToPendingOrders() {
        try {
            List<Order> pendingOrders = orderRepository.findPendingOrdersForAssignment();
            
            for (Order order : pendingOrders) {
                assignDriverToOrder(order);
            }
            
            if (!pendingOrders.isEmpty()) {
                System.out.println("Processed " + pendingOrders.size() + " pending orders for driver assignment");
            }
            
        } catch (Exception e) {
            System.err.println("Error in scheduled driver assignment: " + e.getMessage());
        }
    }
    
    /**
     * Manually assign a specific driver to an order
     */
    public boolean assignSpecificDriverToOrder(Long orderId, Long driverId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            Driver driver = driverRepository.findById(driverId)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));
            
            if (driver.getStatus() != Driver.DriverStatus.ONLINE) {
                throw new RuntimeException("Driver is not available");
            }
            
            if (order.getDriver() != null) {
                throw new RuntimeException("Order already has a driver assigned");
            }
            
            // Assign driver
            order.setDriver(driver);
            order.setStatus(Order.OrderStatus.PICKED_UP);
            driver.setStatus(Driver.DriverStatus.ON_DELIVERY);
            
            orderRepository.save(order);
            driverRepository.save(driver);
            
            // Publish assignment event
            kafkaTemplate.send("driver-assignments", "manual-assignment",
                    String.format("{\"orderId\":%d,\"driverId\":%d}", orderId, driverId));
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error in manual driver assignment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Unassign driver from order (e.g., driver cancellation)
     */
    public boolean unassignDriverFromOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            if (order.getDriver() == null) {
                throw new RuntimeException("Order has no driver assigned");
            }
            
            Driver driver = order.getDriver();
            
            // Unassign driver
            order.setDriver(null);
            order.setStatus(Order.OrderStatus.READY_FOR_PICKUP);
            driver.setStatus(Driver.DriverStatus.ONLINE);
            
            orderRepository.save(order);
            driverRepository.save(driver);
            
            // Publish unassignment event
            kafkaTemplate.send("driver-assignments", "assignment-cancelled",
                    String.format("{\"orderId\":%d,\"driverId\":%d}", orderId, driver.getId()));
            
            // Try to find another driver
            assignDriverToOrder(order);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error in driver unassignment: " + e.getMessage());
            return false;
        }
    }
}
