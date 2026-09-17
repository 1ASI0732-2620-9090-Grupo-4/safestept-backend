package com.safestep.platform.commerce.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.RedemptionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class RedeemedCoupon extends AbstractDomainAggregateRoot<RedeemedCoupon> {
    private Long id;
    private final String externalId, username, couponId, title;
    private final CouponType type;
    private final int discountPercentage;
    private final BigDecimal minPurchaseAmount;
    private final Instant redeemedAt;
    private Instant usedAt;
    private RedemptionStatus status;

    public RedeemedCoupon(Long id, String externalId, String username, String couponId, String title, CouponType type,
            int discountPercentage, BigDecimal minPurchaseAmount, Instant redeemedAt, Instant usedAt,
            RedemptionStatus status) {
        this.id = id;
        this.externalId = externalId;
        this.username = username;
        this.couponId = couponId;
        this.title = title;
        this.type = type == null ? CouponType.PERCENTAGE_OFF : type;
        this.discountPercentage = discountPercentage;
        this.minPurchaseAmount = minPurchaseAmount;
        this.redeemedAt = redeemedAt;
        this.usedAt = usedAt;
        this.status = status == null ? RedemptionStatus.AVAILABLE : status;
    }

    public void markUsed(Instant at) {
        if (status != RedemptionStatus.AVAILABLE)
            throw new IllegalStateException("Redeemed coupon is not available");
        status = RedemptionStatus.USED;
        usedAt = at;
    }

    public void release() {
        status = RedemptionStatus.AVAILABLE;
        usedAt = null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public String getUsername() { return username; }
    public String getCouponId() { return couponId; }
    public String getTitle() { return title; }
    public CouponType getType() { return type; }
    public int getDiscountPercentage() { return discountPercentage; }
    public BigDecimal getMinPurchaseAmount() { return minPurchaseAmount; }
    public Instant getRedeemedAt() { return redeemedAt; }
    public Instant getUsedAt() { return usedAt; }
    public RedemptionStatus getStatus() { return status; }
}
