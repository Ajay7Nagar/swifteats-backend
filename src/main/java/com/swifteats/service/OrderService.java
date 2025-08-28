package com.swifteats.service;

import com.swifteats.dto.CreateOrderRequestDto;
import com.swifteats.dto.OrderResponseDto;
import com.swifteats.exception.RateLimitExceededException;
import com.swifteats.model.*;
import com.swifteats.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for Order management operations
 * Optimized for high-volume processing (500 orders/min)
 */
@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final DriverRepository driverRepository;
    private final PaymentService paymentService;
    private final DriverAssignmentService driverAssignmentService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${swifteats.order.max-processing-rate}")
    private int maxOrdersPerMinute;
    
    @Autowired
    public OrderService(OrderRepository orderRepository,
                       CustomerRepository customerRepository,
                       RestaurantRepository restaurantRepository,
                       MenuItemRepository menuItemRepository,
                       DriverRepository driverRepository,
                       PaymentService paymentService,
                       DriverAssignmentService driverAssignmentService,
                       KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.driverRepository = driverRepository;
        this.paymentService = paymentService;
        this.driverAssignmentService = driverAssignmentService;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Create new order - optimized for high throughput
     */
    public OrderResponseDto createOrder(CreateOrderRequestDto request) {
        // Rate limiting check
        validateOrderRate();
        
        // Validate entities exist
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        if (restaurant.getStatus() != Restaurant.RestaurantStatus.ACTIVE) {
            throw new RuntimeException("Restaurant is not available for orders");
        }
        
        // Calculate order amount
        double totalAmount = 0.0;
        Order order = new Order(customer, restaurant, totalAmount, request.getDeliveryAddress());
        order.setDeliveryLatitude(request.getDeliveryLatitude());
        order.setDeliveryLongitude(request.getDeliveryLongitude());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setDeliveryFee(restaurant.getDeliveryFee());
        
        // Save order first to get ID
        order = orderRepository.save(order);
        
        // Process order items
        for (CreateOrderRequestDto.OrderItemRequestDto itemRequest : request.getOrderItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));
            
            if (!menuItem.getAvailable()) {
                throw new RuntimeException("Menu item is not available: " + menuItem.getName());
            }
            
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new RuntimeException("Menu item does not belong to the specified restaurant");
            }
            
            OrderItem orderItem = new OrderItem(order, menuItem, itemRequest.getQuantity(), menuItem.getPrice());
            orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
            order.getOrderItems().add(orderItem);
            
            totalAmount += orderItem.getTotalPrice();
        }
        
        // Calculate final amounts
        double taxAmount = totalAmount * 0.18; // 18% tax
        order.setTotalAmount(totalAmount + order.getDeliveryFee() + taxAmount);
        order.setTaxAmount(taxAmount);
        
        // Estimate delivery time
        LocalDateTime estimatedDelivery = LocalDateTime.now()
                .plusMinutes(restaurant.getAverageDeliveryTime() != null ? 
                           restaurant.getAverageDeliveryTime() : 30);
        order.setEstimatedDeliveryTime(estimatedDelivery);
        
        // Save updated order
        order = orderRepository.save(order);
        
        // Process payment asynchronously to improve throughput
        final Order finalOrder = order;
        CompletableFuture.supplyAsync(() -> {
            try {
                boolean paymentSuccess = paymentService.processPayment(finalOrder);
                if (paymentSuccess) {
                    finalOrder.setPaymentStatus(Order.PaymentStatus.COMPLETED);
                    finalOrder.setStatus(Order.OrderStatus.CONFIRMED);
                    orderRepository.save(finalOrder);
                    
                    // Publish order confirmation event
                    kafkaTemplate.send("order-events", "order-confirmed", finalOrder.getId().toString());
                } else {
                    finalOrder.setPaymentStatus(Order.PaymentStatus.FAILED);
                    finalOrder.setStatus(Order.OrderStatus.CANCELLED);
                    orderRepository.save(finalOrder);
                }
            } catch (Exception e) {
                // Handle payment processing error
                finalOrder.setPaymentStatus(Order.PaymentStatus.FAILED);
                finalOrder.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(finalOrder);
            }
            return null;
        });
        
        return new OrderResponseDto(order);
    }
    
    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return new OrderResponseDto(order);
    }
    
    /**
     * Get customer orders with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getCustomerOrders(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        return orders.map(OrderResponseDto::new);
    }
    
    /**
     * Get restaurant orders with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getRestaurantOrders(Long restaurantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable);
        return orders.map(OrderResponseDto::new);
    }
    
    /**
     * Update order status
     */
    public OrderResponseDto updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        
        // Handle specific status updates
        if (newStatus == Order.OrderStatus.READY_FOR_PICKUP) {
            // Try to assign driver
            driverAssignmentService.assignDriverToOrder(order);
        } else if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setActualDeliveryTime(LocalDateTime.now());
        }
        
        order = orderRepository.save(order);
        
        // Publish status update event
        kafkaTemplate.send("order-events", "status-updated", 
                          String.format("{\"orderId\":%d,\"status\":\"%s\"}", orderId, newStatus));
        
        return new OrderResponseDto(order);
    }
    
    /**
     * Assign driver to order
     */
    public OrderResponseDto assignDriver(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        
        if (driver.getStatus() != Driver.DriverStatus.ONLINE) {
            throw new RuntimeException("Driver is not available");
        }
        
        order.setDriver(driver);
        order.setStatus(Order.OrderStatus.PICKED_UP);
        driver.setStatus(Driver.DriverStatus.ON_DELIVERY);
        
        orderRepository.save(order);
        driverRepository.save(driver);
        
        // Publish driver assignment event
        kafkaTemplate.send("order-events", "driver-assigned", 
                          String.format("{\"orderId\":%d,\"driverId\":%d}", orderId, driverId));
        
        return new OrderResponseDto(order);
    }
    
    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Rate limiting validation
     */
    private void validateOrderRate() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        // For simplicity, checking last hour instead of implementing sliding window
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentOrders = orderRepository.countOrdersInLastHour(oneHourAgo);
        
        // Simple rate limiting - in production, use Redis-based sliding window
        if (recentOrders > maxOrdersPerMinute * 60) {
            throw new RateLimitExceededException();
        }
    }
    
    /**
     * Validate order status transitions
     */
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Define valid transitions
        switch (currentStatus) {
            case PLACED:
                if (newStatus != Order.OrderStatus.CONFIRMED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition");
                }
                break;
            case CONFIRMED:
                if (newStatus != Order.OrderStatus.PREPARING && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition");
                }
                break;
            case PREPARING:
                if (newStatus != Order.OrderStatus.READY_FOR_PICKUP && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition");
                }
                break;
            case READY_FOR_PICKUP:
                if (newStatus != Order.OrderStatus.PICKED_UP && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition");
                }
                break;
            case PICKED_UP:
                if (newStatus != Order.OrderStatus.OUT_FOR_DELIVERY && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition");
                }
                break;
            case OUT_FOR_DELIVERY:
                if (newStatus != Order.OrderStatus.DELIVERED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition");
                }
                break;
            default:
                throw new RuntimeException("Invalid status transition");
        }
    }
}
