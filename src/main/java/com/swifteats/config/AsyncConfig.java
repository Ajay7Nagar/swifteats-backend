package com.swifteats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for non-blocking operations
 * Optimized for high-throughput order processing and location updates
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Thread pool for general async operations
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Minimum number of threads
        executor.setMaxPoolSize(50);  // Maximum number of threads
        executor.setQueueCapacity(200); // Queue capacity for pending tasks
        executor.setThreadNamePrefix("SwiftEats-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }
    
    /**
     * Dedicated thread pool for location updates (high frequency)
     */
    @Bean(name = "locationUpdateExecutor")
    public Executor locationUpdateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // Higher core pool for location updates
        executor.setMaxPoolSize(100); // Higher max pool for peak loads
        executor.setQueueCapacity(1000); // Larger queue for location updates
        executor.setThreadNamePrefix("Location-Update-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
    
    /**
     * Thread pool for payment processing
     */
    @Bean(name = "paymentExecutor")
    public Executor paymentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Payment-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
    
    /**
     * Thread pool for driver assignment operations
     */
    @Bean(name = "driverAssignmentExecutor")
    public Executor driverAssignmentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Driver-Assignment-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        executor.initialize();
        return executor;
    }
}
