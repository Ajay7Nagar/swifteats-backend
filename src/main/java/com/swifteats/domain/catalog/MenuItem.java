package com.swifteats.domain.catalog;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class MenuItem {
    private UUID id;
    private UUID restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean available;
    private OffsetDateTime updatedAt;

    public MenuItem(UUID id, UUID restaurantId, String name, String description, BigDecimal price, boolean available, OffsetDateTime updatedAt) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getRestaurantId() { return restaurantId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public boolean isAvailable() { return available; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}


