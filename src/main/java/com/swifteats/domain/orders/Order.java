package com.swifteats.domain.orders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class Order {
    private UUID id;
    private UUID customerId;
    private UUID restaurantId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String currency;
    private OffsetDateTime createdAt;
    private int version;

    public Order(UUID id,
                 UUID customerId,
                 UUID restaurantId,
                 OrderStatus status,
                 PaymentStatus paymentStatus,
                 List<OrderItem> items,
                 BigDecimal totalAmount,
                 String currency,
                 OffsetDateTime createdAt,
                 int version) {
        this.id = id;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.items = items;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.createdAt = createdAt;
        this.version = version;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getRestaurantId() { return restaurantId; }
    public OrderStatus getStatus() { return status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public int getVersion() { return version; }
}


