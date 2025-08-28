package com.swifteats.controller;

import com.swifteats.dto.CreateOrderRequestDto;
import com.swifteats.dto.OrderResponseDto;
import com.swifteats.model.Order;
import com.swifteats.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Order management operations
 * Optimized for high-volume processing (500 orders/min)
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "APIs for order processing and management")
public class OrderController {
    
    private final OrderService orderService;
    
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * Create a new order
     */
    @PostMapping
    @Operation(summary = "Create new order", description = "Create a new food order with items and delivery details")
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequestDto request) {
        try {
            OrderResponseDto order = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by order ID")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        
        try {
            OrderResponseDto order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get customer orders with pagination
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer orders", description = "Retrieve paginated list of orders for a customer")
    public ResponseEntity<Page<OrderResponseDto>> getCustomerOrders(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Page<OrderResponseDto> orders = orderService.getCustomerOrders(customerId, page, size);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get restaurant orders with pagination
     */
    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get restaurant orders", description = "Retrieve paginated list of orders for a restaurant")
    public ResponseEntity<Page<OrderResponseDto>> getRestaurantOrders(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Page<OrderResponseDto> orders = orderService.getRestaurantOrders(restaurantId, page, size);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Update order status
     */
    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an existing order")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "New order status") @RequestParam String status) {
        
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            OrderResponseDto order = orderService.updateOrderStatus(orderId, orderStatus);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Assign driver to order
     */
    @PutMapping("/{orderId}/assign-driver")
    @Operation(summary = "Assign driver to order", description = "Manually assign a driver to an order")
    public ResponseEntity<OrderResponseDto> assignDriver(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Driver ID") @RequestParam Long driverId) {
        
        try {
            OrderResponseDto order = orderService.assignDriver(orderId, driverId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get orders by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Retrieve all orders with specific status")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable String status) {
        
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<OrderResponseDto> orders = orderService.getOrdersByStatus(orderStatus);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all pending orders (for restaurant dashboard)
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending orders", description = "Retrieve all orders waiting to be processed")
    public ResponseEntity<List<OrderResponseDto>> getPendingOrders() {
        List<OrderResponseDto> orders = orderService.getOrdersByStatus(Order.OrderStatus.PLACED);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get all confirmed orders (for restaurant dashboard)
     */
    @GetMapping("/confirmed")
    @Operation(summary = "Get confirmed orders", description = "Retrieve all confirmed orders")
    public ResponseEntity<List<OrderResponseDto>> getConfirmedOrders() {
        List<OrderResponseDto> orders = orderService.getOrdersByStatus(Order.OrderStatus.CONFIRMED);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get orders ready for pickup (for driver assignment)
     */
    @GetMapping("/ready-for-pickup")
    @Operation(summary = "Get orders ready for pickup", description = "Retrieve orders ready for driver pickup")
    public ResponseEntity<List<OrderResponseDto>> getOrdersReadyForPickup() {
        List<OrderResponseDto> orders = orderService.getOrdersByStatus(Order.OrderStatus.READY_FOR_PICKUP);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get out for delivery orders (for tracking)
     */
    @GetMapping("/out-for-delivery")
    @Operation(summary = "Get orders out for delivery", description = "Retrieve orders currently being delivered")
    public ResponseEntity<List<OrderResponseDto>> getOrdersOutForDelivery() {
        List<OrderResponseDto> orders = orderService.getOrdersByStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        return ResponseEntity.ok(orders);
    }
}
