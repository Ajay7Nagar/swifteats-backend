package com.swifteats.websocket;

import com.swifteats.dto.DriverLocationUpdateDto;
import com.swifteats.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * STOMP WebSocket controller for real-time tracking features
 * Handles live driver location updates and order tracking
 */
@Controller
public class LiveTrackingController {
    
    private final DriverService driverService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public LiveTrackingController(DriverService driverService, SimpMessagingTemplate messagingTemplate) {
        this.driverService = driverService;
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Handle driver location updates via STOMP
     * Clients send location updates to /app/driver/location
     */
    @MessageMapping("/driver/location")
    public void handleDriverLocationUpdate(DriverLocationUpdateDto locationUpdate) {
        try {
            // Set timestamp if not provided
            if (locationUpdate.getTimestamp() == null) {
                locationUpdate.setTimestamp(LocalDateTime.now());
            }
            
            // Process location update
            driverService.updateDriverLocation(locationUpdate);
            
            // Broadcast to subscribers - handled by DriverService via SimpMessagingTemplate
            System.out.println("Processed location update for driver: " + locationUpdate.getDriverId());
            
        } catch (Exception e) {
            System.err.println("Error processing STOMP location update: " + e.getMessage());
        }
    }
    
    /**
     * Subscribe to driver location updates
     * Clients subscribe to /topic/driver-location/{driverId}
     */
    @MessageMapping("/subscribe/driver/{driverId}")
    @SendTo("/topic/driver-location/{driverId}")
    public Map<String, Object> subscribeToDriver(@DestinationVariable Long driverId) {
        return Map.of(
            "type", "subscription_confirmed",
            "driverId", driverId,
            "message", "Subscribed to driver location updates",
            "timestamp", System.currentTimeMillis()
        );
    }
    
    /**
     * Subscribe to order tracking updates
     * Clients subscribe to /topic/order-tracking/{orderId}
     */
    @MessageMapping("/subscribe/order/{orderId}")
    @SendTo("/topic/order-tracking/{orderId}")
    public Map<String, Object> subscribeToOrder(@DestinationVariable Long orderId) {
        return Map.of(
            "type", "subscription_confirmed",
            "orderId", orderId,
            "message", "Subscribed to order tracking updates",
            "timestamp", System.currentTimeMillis()
        );
    }
    
    /**
     * Handle ping messages for connection health check
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public Map<String, Object> handlePing() {
        return Map.of(
            "type", "pong",
            "timestamp", System.currentTimeMillis(),
            "message", "Connection is alive"
        );
    }
    
    /**
     * Broadcast driver location update to all subscribers
     */
    public void broadcastDriverLocation(Long driverId, DriverLocationUpdateDto locationUpdate) {
        Map<String, Object> locationData = Map.of(
            "type", "location_update",
            "driverId", driverId,
            "latitude", locationUpdate.getLatitude(),
            "longitude", locationUpdate.getLongitude(),
            "speed", locationUpdate.getSpeed() != null ? locationUpdate.getSpeed() : 0.0,
            "heading", locationUpdate.getHeading() != null ? locationUpdate.getHeading() : 0.0,
            "accuracy", locationUpdate.getAccuracy() != null ? locationUpdate.getAccuracy() : 0.0,
            "timestamp", System.currentTimeMillis()
        );
        
        // Send to specific driver topic
        messagingTemplate.convertAndSend("/topic/driver-location/" + driverId, locationData);
        
        // Send to general driver updates topic for dashboards
        messagingTemplate.convertAndSend("/topic/driver-updates", locationData);
    }
    
    /**
     * Broadcast order status update to subscribers
     */
    public void broadcastOrderUpdate(Long orderId, String status, Long driverId, String message) {
        Map<String, Object> orderData = Map.of(
            "type", "order_update",
            "orderId", orderId,
            "status", status,
            "driverId", driverId != null ? driverId : "",
            "message", message != null ? message : "",
            "timestamp", System.currentTimeMillis()
        );
        
        // Send to specific order topic
        messagingTemplate.convertAndSend("/topic/order-tracking/" + orderId, orderData);
        
        // Send to general order updates topic for restaurant dashboards
        messagingTemplate.convertAndSend("/topic/order-updates", orderData);
    }
    
    /**
     * Broadcast driver status change
     */
    public void broadcastDriverStatusChange(Long driverId, String status) {
        Map<String, Object> statusData = Map.of(
            "type", "driver_status_change",
            "driverId", driverId,
            "status", status,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/driver-status/" + driverId, statusData);
        messagingTemplate.convertAndSend("/topic/driver-updates", statusData);
    }
    
    /**
     * Send system notification to all connected clients
     */
    public void broadcastSystemNotification(String message, String level) {
        Map<String, Object> notification = Map.of(
            "type", "system_notification",
            "message", message,
            "level", level, // info, warning, error
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}

