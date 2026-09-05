package com.safestep.platform.commerce.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "cart_items")
public class CartItemPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true)
    private String externalId;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String productId;
    private int quantity;
    private Instant addedAt;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String v) {
        externalId = v;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        username = v;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String v) {
        productId = v;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int v) {
        quantity = v;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant v) {
        addedAt = v;
    }
}
