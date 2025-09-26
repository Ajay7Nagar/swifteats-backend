package com.swifteats.adapters.web.orders;

import com.swifteats.application.orders.OrderService;
import com.swifteats.domain.orders.OrderAction;
import com.swifteats.domain.orders.Order;
import com.swifteats.domain.orders.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderCreateResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(i -> new OrderItem(i.menuItemId(), i.nameSnapshot(), i.quantity(), i.unitPrice(), i.totalPrice()))
                .toList();
        Order order = orderService.createOrder(request.customerId(), request.restaurantId(), items, request.currency());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrderCreateResponse(order.getId(), order.getStatus().name()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> get(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PostMapping("/{orderId}/transition")
    public ResponseEntity<Order> transition(@PathVariable UUID orderId, @RequestBody TransitionRequest req) {
        Order updated = orderService.applyTransition(orderId, req.action());
        return ResponseEntity.ok(updated);
    }

    public record OrderCreateRequest(
            @NotNull UUID customerId,
            @NotNull UUID restaurantId,
            @NotBlank String currency,
            @NotNull List<OrderItemRequest> items
    ) {}

    public record OrderItemRequest(
            @NotNull UUID menuItemId,
            @NotBlank String nameSnapshot,
            @Positive int quantity,
            @NotNull BigDecimal unitPrice,
            @NotNull BigDecimal totalPrice
    ) {}

    public record OrderCreateResponse(UUID orderId, String status) {}

    public record TransitionRequest(OrderAction action) {}
}


