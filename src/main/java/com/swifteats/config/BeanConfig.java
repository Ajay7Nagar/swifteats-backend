package com.swifteats.config;

import com.swifteats.application.orders.OrderService;
import com.swifteats.application.catalog.CatalogService;
import com.swifteats.application.drivertracking.DriverTrackingService;
import com.swifteats.application.user.AuthService;
import com.swifteats.domain.catalog.CatalogRepository;
import com.swifteats.domain.drivertracking.DriverTrackingRepository;
import com.swifteats.domain.orders.OrderRepository;
import com.swifteats.domain.orders.PaymentPublisher;
import com.swifteats.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableCaching
public class BeanConfig {

    @Bean
    public OrderService orderService(OrderRepository orderRepository, PaymentPublisher paymentPublisher) {
        return new OrderService(orderRepository, paymentPublisher);
    }

    @Bean
    public CatalogService catalogService(CatalogRepository catalogRepository) {
        return new CatalogService(catalogRepository);
    }

    @Bean
    public DriverTrackingService driverTrackingService(DriverTrackingRepository repository) {
        return new DriverTrackingService(repository);
    }

    @Bean
    public AuthService authService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${security.jwt.secret:ZmFrZV9kZWFkYmVlZjEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIz}") String jwtSecret,
                                   @Value("${security.jwt.ttlSeconds:3600}") long ttlSeconds) {
        return new AuthService(userRepository, passwordEncoder, jwtSecret, ttlSeconds);
    }
}


