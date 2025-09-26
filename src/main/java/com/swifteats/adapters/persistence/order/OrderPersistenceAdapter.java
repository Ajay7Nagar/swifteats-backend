package com.swifteats.adapters.persistence.order;

import com.swifteats.domain.orders.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        OrderEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        return jpaRepository.findById(orderId).map(this::toDomain);
    }

    private OrderEntity toEntity(Order order) {
        OrderEntity e = new OrderEntity();
        e.setId(order.getId());
        e.setCustomerId(order.getCustomerId());
        e.setRestaurantId(order.getRestaurantId());
        e.setStatus(order.getStatus().name());
        e.setPaymentStatus(order.getPaymentStatus().name());
        e.setTotalAmount(order.getTotalAmount());
        e.setCurrency(order.getCurrency());
        e.setCreatedAt(order.getCreatedAt());
        e.setVersion(order.getVersion());

        List<OrderItemEntity> items = order.getItems().stream().map(i -> {
            OrderItemEntity ie = new OrderItemEntity();
            ie.setId(UUID.randomUUID());
            ie.setOrder(e);
            ie.setMenuItemId(i.getMenuItemId());
            ie.setNameSnapshot(i.getNameSnapshot());
            ie.setQuantity(i.getQuantity());
            ie.setUnitPrice(i.getUnitPrice());
            ie.setTotalPrice(i.getTotalPrice());
            return ie;
        }).collect(Collectors.toList());
        e.setItems(items);
        return e;
    }

    private Order toDomain(OrderEntity e) {
        List<OrderItem> items = e.getItems().stream().map(ie -> new OrderItem(
                ie.getMenuItemId(),
                ie.getNameSnapshot(),
                ie.getQuantity(),
                ie.getUnitPrice(),
                ie.getTotalPrice()
        )).collect(Collectors.toList());
        return new Order(
                e.getId(),
                e.getCustomerId(),
                e.getRestaurantId(),
                OrderStatus.valueOf(e.getStatus()),
                PaymentStatus.valueOf(e.getPaymentStatus()),
                items,
                e.getTotalAmount(),
                e.getCurrency(),
                e.getCreatedAt(),
                e.getVersion()
        );
    }
}


