package com.safestep.platform.commerce.domain.model.entities;

import java.time.*;

public class CartItem {
    private Long id;
    private final String externalId, username, productId;
    private int quantity;
    private final Instant addedAt;

    public CartItem(Long id, String externalId, String username, String productId, int quantity, Instant at) {
        if (quantity < 1)
            throw new IllegalArgumentException("Quantity must be at least one");
        this.id = id;
        this.externalId = externalId;
        this.username = username;
        this.productId = productId;
        this.quantity = quantity;
        addedAt = at;
    }

    public void changeQuantity(int v) {
        if (v < 1)
            throw new IllegalArgumentException("Quantity must be at least one");
        quantity = v;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        id = v;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getUsername() {
        return username;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
