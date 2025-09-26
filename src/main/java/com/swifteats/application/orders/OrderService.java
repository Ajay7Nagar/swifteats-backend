package com.swifteats.application.orders;

import com.swifteats.domain.orders.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentPublisher paymentPublisher;

    public OrderService(OrderRepository orderRepository, PaymentPublisher paymentPublisher) {
        this.orderRepository = orderRepository;
        this.paymentPublisher = paymentPublisher;
    }

    public Order createOrder(UUID customerId,
                             UUID restaurantId,
                             List<OrderItem> items,
                             String currency) {
        BigDecimal total = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(
                UUID.randomUUID(),
                customerId,
                restaurantId,
                OrderStatus.created,
                PaymentStatus.pending,
                items,
                total,
                currency,
                OffsetDateTime.now(),
                0
        );
        Order saved = orderRepository.save(order);
        paymentPublisher.publishPaymentRequested(saved.getId(), saved.getTotalAmount(), saved.getCurrency());
        return saved;
    }

    public Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Order not found"));
    }

    public Order applyTransition(UUID orderId, OrderAction action) {
        Order current = getOrder(orderId);
        OrderStatus next = nextStatus(current.getStatus(), action);
        if (next == null) {
            throw new IllegalStateException("INVALID_TRANSITION");
        }
        Order updated = new Order(
                current.getId(),
                current.getCustomerId(),
                current.getRestaurantId(),
                next,
                current.getPaymentStatus(),
                current.getItems(),
                current.getTotalAmount(),
                current.getCurrency(),
                current.getCreatedAt(),
                current.getVersion()
        );
        return orderRepository.save(updated);
    }

    private OrderStatus nextStatus(OrderStatus current, OrderAction action) {
        switch (current) {
            case created:
                if (action == OrderAction.confirm) return OrderStatus.confirmed;
                if (action == OrderAction.cancel) return OrderStatus.cancelled;
                break;
            case confirmed:
                if (action == OrderAction.assign_driver) return OrderStatus.driver_assigned;
                if (action == OrderAction.cancel) return OrderStatus.cancelled;
                break;
            case driver_assigned:
                if (action == OrderAction.pick_up) return OrderStatus.picked_up;
                if (action == OrderAction.cancel) return OrderStatus.cancelled;
                break;
            case picked_up:
                if (action == OrderAction.deliver) return OrderStatus.delivered;
                if (action == OrderAction.cancel) return OrderStatus.cancelled;
                break;
            case delivered:
                if (action == OrderAction.complete) return OrderStatus.completed;
                break;
            default:
                break;
        }
        return null;
    }
}


