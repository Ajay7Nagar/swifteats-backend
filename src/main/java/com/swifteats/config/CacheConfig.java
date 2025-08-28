package com.swifteats.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration for high-performance menu/restaurant browsing
 * Optimized for <200ms P99 response time
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // Default 5 minutes TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
        
        // Specific cache configurations with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Restaurant cache - 10 minutes (restaurants don't change frequently)
        cacheConfigurations.put("restaurants", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Menu cache - 5 minutes (menus might update during day)
        cacheConfigurations.put("menus", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Categories cache - 30 minutes (categories are relatively static)
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Driver cache - 1 minute (driver status changes frequently)
        cacheConfigurations.put("drivers", defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        // Location cache - 30 seconds (locations change very frequently)
        cacheConfigurations.put("locations", defaultConfig.entryTtl(Duration.ofSeconds(30)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
