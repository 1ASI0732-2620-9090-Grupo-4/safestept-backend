package com.safestep.platform.commerce.domain.model.entities;

import java.math.*;

public record OrderItem(String productId, String productName, BigDecimal unitPrice, int quantity) {
    public OrderItem {
        if (quantity < 1)
            throw new IllegalArgumentException("Quantity must be at least one");
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
