package com.swifteats.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swifteats.dto.DriverLocationUpdateDto;
import com.swifteats.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time driver location updates
 * Manages WebSocket connections for live driver tracking
 */
@Component
public class DriverLocationWebSocketHandler implements WebSocketHandler {
    
    private final DriverService driverService;
    private final ObjectMapper objectMapper;
    
    // Store active WebSocket sessions for real-time updates
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    @Autowired
    public DriverLocationWebSocketHandler(DriverService driverService, ObjectMapper objectMapper) {
        this.driverService = driverService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        activeSessions.put(sessionId, session);
        
        System.out.println("WebSocket connection established: " + sessionId);
        
        // Send welcome message
        String welcomeMessage = objectMapper.writeValueAsString(Map.of(
            "type", "connection_established",
            "sessionId", sessionId,
            "timestamp", System.currentTimeMillis()
        ));
        
        session.sendMessage(new TextMessage(welcomeMessage));
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMessage) {
            try {
                // Parse incoming message
                Map<String, Object> messageData = objectMapper.readValue(textMessage.getPayload(), Map.class);
                String messageType = (String) messageData.get("type");
                
                switch (messageType) {
                    case "location_update":
                        handleLocationUpdate(session, messageData);
                        break;
                    case "subscribe_driver":
                        handleDriverSubscription(session, messageData);
                        break;
                    case "subscribe_order":
                        handleOrderSubscription(session, messageData);
                        break;
                    case "ping":
                        handlePing(session);
                        break;
                    default:
                        sendErrorMessage(session, "Unknown message type: " + messageType);
                }
            } catch (Exception e) {
                sendErrorMessage(session, "Error processing message: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error for session " + session.getId() + ": " + exception.getMessage());
        session.close();
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        activeSessions.remove(sessionId);
        
        System.out.println("WebSocket connection closed: " + sessionId + 
                          " with status: " + closeStatus.getCode() + " - " + closeStatus.getReason());
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * Handle driver location update messages
     */
    private void handleLocationUpdate(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        try {
            // Extract location data
            Long driverId = Long.valueOf(messageData.get("driverId").toString());
            Double latitude = Double.valueOf(messageData.get("latitude").toString());
            Double longitude = Double.valueOf(messageData.get("longitude").toString());
            
            // Create location update DTO
            DriverLocationUpdateDto locationUpdate = new DriverLocationUpdateDto();
            locationUpdate.setDriverId(driverId);
            locationUpdate.setLatitude(latitude);
            locationUpdate.setLongitude(longitude);
            
            if (messageData.containsKey("accuracy")) {
                locationUpdate.setAccuracy(Double.valueOf(messageData.get("accuracy").toString()));
            }
            if (messageData.containsKey("speed")) {
                locationUpdate.setSpeed(Double.valueOf(messageData.get("speed").toString()));
            }
            if (messageData.containsKey("heading")) {
                locationUpdate.setHeading(Double.valueOf(messageData.get("heading").toString()));
            }
            
            // Process location update
            driverService.updateDriverLocation(locationUpdate);
            
            // Send confirmation
            String confirmationMessage = objectMapper.writeValueAsString(Map.of(
                "type", "location_update_confirmed",
                "driverId", driverId,
                "timestamp", System.currentTimeMillis()
            ));
            
            session.sendMessage(new TextMessage(confirmationMessage));
            
            // Broadcast to subscribed clients
            broadcastLocationUpdate(driverId, locationUpdate);
            
        } catch (Exception e) {
            sendErrorMessage(session, "Error processing location update: " + e.getMessage());
        }
    }
    
    /**
     * Handle driver subscription for location updates
     */
    private void handleDriverSubscription(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Long driverId = Long.valueOf(messageData.get("driverId").toString());
        
        // Store subscription in session attributes
        session.getAttributes().put("subscribed_driver", driverId);
        
        String confirmationMessage = objectMapper.writeValueAsString(Map.of(
            "type", "driver_subscription_confirmed",
            "driverId", driverId,
            "timestamp", System.currentTimeMillis()
        ));
        
        session.sendMessage(new TextMessage(confirmationMessage));
    }
    
    /**
     * Handle order subscription for tracking updates
     */
    private void handleOrderSubscription(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Long orderId = Long.valueOf(messageData.get("orderId").toString());
        
        // Store subscription in session attributes
        session.getAttributes().put("subscribed_order", orderId);
        
        String confirmationMessage = objectMapper.writeValueAsString(Map.of(
            "type", "order_subscription_confirmed",
            "orderId", orderId,
            "timestamp", System.currentTimeMillis()
        ));
        
        session.sendMessage(new TextMessage(confirmationMessage));
    }
    
    /**
     * Handle ping messages for connection health check
     */
    private void handlePing(WebSocketSession session) throws Exception {
        String pongMessage = objectMapper.writeValueAsString(Map.of(
            "type", "pong",
            "timestamp", System.currentTimeMillis()
        ));
        
        session.sendMessage(new TextMessage(pongMessage));
    }
    
    /**
     * Send error message to client
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            String errorResponse = objectMapper.writeValueAsString(Map.of(
                "type", "error",
                "message", errorMessage,
                "timestamp", System.currentTimeMillis()
            ));
            
            session.sendMessage(new TextMessage(errorResponse));
        } catch (IOException e) {
            System.err.println("Failed to send error message: " + e.getMessage());
        }
    }
    
    /**
     * Broadcast location update to subscribed clients
     */
    public void broadcastLocationUpdate(Long driverId, DriverLocationUpdateDto locationUpdate) {
        String locationMessage;
        try {
            locationMessage = objectMapper.writeValueAsString(Map.of(
                "type", "driver_location_update",
                "driverId", driverId,
                "latitude", locationUpdate.getLatitude(),
                "longitude", locationUpdate.getLongitude(),
                "speed", locationUpdate.getSpeed(),
                "heading", locationUpdate.getHeading(),
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            System.err.println("Error creating location broadcast message: " + e.getMessage());
            return;
        }
        
        // Send to all clients subscribed to this driver
        activeSessions.values().parallelStream().forEach(session -> {
            try {
                Long subscribedDriverId = (Long) session.getAttributes().get("subscribed_driver");
                if (subscribedDriverId != null && subscribedDriverId.equals(driverId)) {
                    session.sendMessage(new TextMessage(locationMessage));
                }
            } catch (Exception e) {
                System.err.println("Error broadcasting location update: " + e.getMessage());
            }
        });
    }
    
    /**
     * Broadcast order status update to subscribed clients
     */
    public void broadcastOrderUpdate(Long orderId, String status, Long driverId) {
        String orderMessage;
        try {
            Map<String, Object> messageData = Map.of(
                "type", "order_status_update",
                "orderId", orderId,
                "status", status,
                "driverId", driverId != null ? driverId : "",
                "timestamp", System.currentTimeMillis()
            );
            orderMessage = objectMapper.writeValueAsString(messageData);
        } catch (Exception e) {
            System.err.println("Error creating order broadcast message: " + e.getMessage());
            return;
        }
        
        // Send to all clients subscribed to this order
        activeSessions.values().parallelStream().forEach(session -> {
            try {
                Long subscribedOrderId = (Long) session.getAttributes().get("subscribed_order");
                if (subscribedOrderId != null && subscribedOrderId.equals(orderId)) {
                    session.sendMessage(new TextMessage(orderMessage));
                }
            } catch (Exception e) {
                System.err.println("Error broadcasting order update: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get count of active WebSocket sessions
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}

