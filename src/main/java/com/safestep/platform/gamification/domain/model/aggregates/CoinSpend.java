package com.safestep.platform.gamification.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.time.Instant;

public class CoinSpend extends AbstractDomainAggregateRoot<CoinSpend> {
    private Long id;
    private final String externalId, username, couponId, couponTitle;
    private final int amount;
    private final Instant spentAt;

    public CoinSpend(Long id, String externalId, String username, String couponId, String couponTitle, int amount,
            Instant spentAt) {
        this.id = id;
        this.externalId = externalId;
        this.username = username;
        this.couponId = couponId;
        this.couponTitle = couponTitle;
        this.amount = amount;
        this.spentAt = spentAt;
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

    public String getCouponId() {
        return couponId;
    }

    public String getCouponTitle() {
        return couponTitle;
    }

    public int getAmount() {
        return amount;
    }

    public Instant getSpentAt() {
        return spentAt;
    }
}
