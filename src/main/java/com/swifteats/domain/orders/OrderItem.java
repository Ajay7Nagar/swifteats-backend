package com.swifteats.domain.orders;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {
    private UUID menuItemId;
    private String nameSnapshot;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public OrderItem(UUID menuItemId, String nameSnapshot, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.menuItemId = menuItemId;
        this.nameSnapshot = nameSnapshot;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public UUID getMenuItemId() { return menuItemId; }
    public String getNameSnapshot() { return nameSnapshot; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}


