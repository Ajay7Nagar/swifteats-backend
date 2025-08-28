package com.swifteats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application class for SwiftEats Food Delivery Platform
 * 
 * Features enabled:
 * - Caching for high-performance menu/restaurant browsing
 * - Kafka for real-time event streaming (driver locations)
 * - Async processing for non-blocking operations
 * - Scheduling for periodic tasks
 */
@SpringBootApplication
@EnableCaching
@EnableKafka
@EnableAsync
@EnableScheduling
public class SwiftEatsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwiftEatsApplication.class, args);
    }
}
