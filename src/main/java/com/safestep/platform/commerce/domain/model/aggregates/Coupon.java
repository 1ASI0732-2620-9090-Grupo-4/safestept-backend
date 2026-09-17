package com.safestep.platform.commerce.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;

import java.math.BigDecimal;

public class Coupon extends AbstractDomainAggregateRoot<Coupon> {
    private Long id;
    private String externalId;
    private String title;
    private int costCoins;
    private CouponType type;
    private int discountPercentage;
    private BigDecimal minPurchaseAmount;

    public Coupon() {}

    public Coupon(Long id, String externalId, String title, int costCoins, CouponType type, int discountPercentage,
            BigDecimal minPurchaseAmount) {
        this.id = id;
        this.externalId = externalId;
        this.title = title;
        this.costCoins = costCoins;
        this.type = type == null ? CouponType.PERCENTAGE_OFF : type;
        this.discountPercentage = discountPercentage;
        this.minPurchaseAmount = this.type == CouponType.PERCENTAGE_OFF_MIN_PURCHASE ? minPurchaseAmount : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public String getTitle() { return title; }
    public int getCostCoins() { return costCoins; }
    public CouponType getType() { return type; }
    public int getDiscountPercentage() { return discountPercentage; }
    public BigDecimal getMinPurchaseAmount() { return minPurchaseAmount; }
}
