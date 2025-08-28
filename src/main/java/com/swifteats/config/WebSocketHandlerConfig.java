package com.swifteats.config;

import com.swifteats.websocket.DriverLocationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuration for raw WebSocket handlers
 * Complements the STOMP WebSocket configuration
 */
@Configuration
@EnableWebSocket
public class WebSocketHandlerConfig implements WebSocketConfigurer {
    
    private final DriverLocationWebSocketHandler driverLocationWebSocketHandler;
    
    @Autowired
    public WebSocketHandlerConfig(DriverLocationWebSocketHandler driverLocationWebSocketHandler) {
        this.driverLocationWebSocketHandler = driverLocationWebSocketHandler;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register raw WebSocket handler for driver location updates
        registry.addHandler(driverLocationWebSocketHandler, "/ws/raw/driver-location")
                .setAllowedOriginPatterns("*") // Allow all origins for development
                .withSockJS(); // Enable SockJS fallback
        
        // Additional endpoint without SockJS for native WebSocket clients
        registry.addHandler(driverLocationWebSocketHandler, "/ws/native/driver-location")
                .setAllowedOriginPatterns("*");
    }
}

