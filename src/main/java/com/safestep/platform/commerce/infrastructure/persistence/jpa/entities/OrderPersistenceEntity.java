package com.safestep.platform.commerce.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "orders")
public class OrderPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true)
    private String externalId;
    @Column(nullable = false)
    private String username;
    private String status;
    private LocalDate orderedAt;
    private String paymentProvider;
    private String paymentStatus;
    private String stripeCheckoutSessionId;
    private String stripePaymentIntentId;
    private Instant paidAt;
    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItemEmbeddable> items = new ArrayList<>();

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public LocalDate getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDate v) {
        orderedAt = v;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(String v) {
        paymentProvider = v;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String v) {
        paymentStatus = v;
    }

    public String getStripeCheckoutSessionId() {
        return stripeCheckoutSessionId;
    }

    public void setStripeCheckoutSessionId(String v) {
        stripeCheckoutSessionId = v;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String v) {
        stripePaymentIntentId = v;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant v) {
        paidAt = v;
    }

    public List<OrderItemEmbeddable> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEmbeddable> v) {
        items = v;
    }

    @Embeddable
    public static class OrderItemEmbeddable {
        private String productId, productName;
        private BigDecimal unitPrice;
        private int quantity;

        public OrderItemEmbeddable() {
        }

        public OrderItemEmbeddable(String id, String name, BigDecimal price, int qty) {
            productId = id;
            productName = name;
            unitPrice = price;
            quantity = qty;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
