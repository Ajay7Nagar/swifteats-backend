package com.swifteats.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swifteats.websocket.LiveTrackingController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka event consumer service for processing real-time events
 * Implements the Analytics, Notification, and Location consumer groups mentioned in architecture
 */
@Service
public class EventConsumerService {
    
    private final ObjectMapper objectMapper;
    private final LiveTrackingController liveTrackingController;
    
    @Autowired
    public EventConsumerService(ObjectMapper objectMapper, LiveTrackingController liveTrackingController) {
        this.objectMapper = objectMapper;
        this.liveTrackingController = liveTrackingController;
    }
    
    /**
     * Analytics Consumer Group: Process driver location events for analytics
     */
    @KafkaListener(topics = "driver-locations", groupId = "analytics-group")
    public void processDriverLocationForAnalytics(String locationData) {
        try {
            JsonNode locationJson = objectMapper.readTree(locationData);
            Long driverId = locationJson.get("driverId").asLong();
            Double latitude = locationJson.get("latitude").asDouble();
            Double longitude = locationJson.get("longitude").asDouble();
            
            // Process for analytics (could store in time-series DB, calculate metrics, etc.)
            System.out.println("Analytics: Driver " + driverId + " location processed for analytics at " +
                             latitude + ", " + longitude);
            
            // Could implement:
            // - Driver route optimization analysis
            // - Heat maps of delivery areas
            // - Performance metrics calculation
            // - Traffic pattern analysis
            
        } catch (Exception e) {
            System.err.println("Error processing driver location for analytics: " + e.getMessage());
        }
    }
    
    /**
     * Location Consumer Group: Process driver location events for live tracking
     */
    @KafkaListener(topics = "driver-locations", groupId = "location-group")
    public void processDriverLocationForLiveTracking(String locationData) {
        try {
            JsonNode locationJson = objectMapper.readTree(locationData);
            Long driverId = locationJson.get("driverId").asLong();
            
            // Broadcast location updates via WebSocket for real-time tracking
            // This enhances the direct WebSocket updates from DriverService
            System.out.println("Live Tracking: Broadcasting driver " + driverId + " location update");
            
            // Could implement additional live tracking features:
            // - ETA calculations
            // - Route optimization
            // - Geofencing alerts
            // - Traffic-aware routing
            
        } catch (Exception e) {
            System.err.println("Error processing driver location for live tracking: " + e.getMessage());
        }
    }
    
    /**
     * Notification Consumer Group: Process order events for notifications
     */
    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void processOrderEventForNotifications(String orderData, String key) {
        try {
            System.out.println("Notification: Processing order event - " + key + ": " + orderData);
            
            switch (key) {
                case "order-confirmed":
                    sendOrderConfirmationNotification(orderData);
                    break;
                case "status-updated":
                    sendOrderStatusUpdateNotification(orderData);
                    break;
                default:
                    System.out.println("Unknown order event type: " + key);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing order event for notifications: " + e.getMessage());
        }
    }
    
    /**
     * Notification Consumer Group: Process driver assignment events
     */
    @KafkaListener(topics = "driver-assignments", groupId = "notification-group")
    public void processDriverAssignmentForNotifications(String assignmentData, String key) {
        try {
            JsonNode assignmentJson = objectMapper.readTree(assignmentData);
            Long orderId = assignmentJson.get("orderId").asLong();
            Long driverId = assignmentJson.get("driverId").asLong();
            
            System.out.println("Notification: Driver " + driverId + " assigned to order " + orderId);
            
            // Broadcast order update via WebSocket
            liveTrackingController.broadcastOrderUpdate(orderId, "PICKED_UP", driverId, 
                "Driver assigned and order picked up");
            
            // Could implement:
            // - Push notifications to customer mobile app
            // - SMS notifications
            // - Email notifications
            // - In-app notifications
            
        } catch (Exception e) {
            System.err.println("Error processing driver assignment for notifications: " + e.getMessage());
        }
    }
    
    /**
     * Analytics Consumer Group: Process order events for business analytics
     */
    @KafkaListener(topics = "order-events", groupId = "analytics-group")
    public void processOrderEventForAnalytics(String orderData, String key) {
        try {
            System.out.println("Analytics: Processing order event for metrics - " + key);
            
            // Could implement:
            // - Order volume metrics
            // - Revenue analytics
            // - Customer behavior analysis
            // - Restaurant performance metrics
            // - Peak time analysis
            // - Geographic order distribution
            
        } catch (Exception e) {
            System.err.println("Error processing order event for analytics: " + e.getMessage());
        }
    }
    
    /**
     * Send order confirmation notification
     */
    private void sendOrderConfirmationNotification(String orderData) {
        // Mock implementation
        System.out.println("📱 Sending order confirmation notification for order: " + orderData);
        // In real implementation:
        // - Send push notification to customer
        // - Send SMS confirmation
        // - Update order tracking in customer app
    }
    
    /**
     * Send order status update notification
     */
    private void sendOrderStatusUpdateNotification(String orderData) {
        try {
            JsonNode orderJson = objectMapper.readTree(orderData);
            Long orderId = orderJson.get("orderId").asLong();
            String status = orderJson.get("status").asText();
            
            System.out.println("📱 Sending status update notification - Order " + orderId + " is now " + status);
            
            // Broadcast via WebSocket
            liveTrackingController.broadcastOrderUpdate(orderId, status, null, 
                "Order status updated to " + status);
            
        } catch (Exception e) {
            System.err.println("Error sending order status notification: " + e.getMessage());
        }
    }
}

