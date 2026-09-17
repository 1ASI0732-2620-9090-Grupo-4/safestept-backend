package com.safestep.platform.gamification.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "coin_spends")
public class CoinSpendPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true)
    private String externalId;
    @Column(nullable = false)
    private String username;
    private String couponId;
    private String couponTitle;
    private int amount;
    private Instant spentAt;

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

    public String getCouponTitle() {
        return couponTitle;
    }

    public void setCouponTitle(String v) {
        couponTitle = v;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int v) {
        amount = v;
    }

    public Instant getSpentAt() {
        return spentAt;
    }

    public void setSpentAt(Instant v) {
        spentAt = v;
    }
}
