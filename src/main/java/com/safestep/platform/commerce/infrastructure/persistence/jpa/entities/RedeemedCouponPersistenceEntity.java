package com.safestep.platform.commerce.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "redeemed_coupons")
public class RedeemedCouponPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(unique = true, nullable = false)
    private String externalId;
    @Column(nullable = false)
    private String username;
    private String couponId;
    private String title;
    private String type;
    private int discountPercentage;
    private BigDecimal minPurchaseAmount;
    private Instant redeemedAt;
    private Instant usedAt;
    private String status;

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

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String v) {
        couponId = v;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getType() {
        return type;
    }

    public void setType(String v) {
        type = v;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int v) {
        discountPercentage = v;
    }

    public BigDecimal getMinPurchaseAmount() {
        return minPurchaseAmount;
    }

    public void setMinPurchaseAmount(BigDecimal v) {
        minPurchaseAmount = v;
    }

    public Instant getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(Instant v) {
        redeemedAt = v;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant v) {
        usedAt = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }
}
