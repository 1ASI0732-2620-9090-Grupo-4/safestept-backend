package com.safestep.platform.commerce.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

public class Coupon extends AbstractDomainAggregateRoot<Coupon> {
    private Long id;
    private String externalId;
    private String title;
    private int costCoins;
    private String discount;

    public Coupon() {}

    public Coupon(Long id, String externalId, String title, int costCoins, String discount) {
        this.id = id;
        this.externalId = externalId;
        this.title = title;
        this.costCoins = costCoins;
        this.discount = discount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public String getTitle() { return title; }
    public int getCostCoins() { return costCoins; }
    public String getDiscount() { return discount; }
}
