package com.safestep.platform.commerce.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "coupons")
public class CouponPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(unique = true, nullable = false)
    private String externalId;
    private String title, discount;
    private int costCoins;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String v) {
        externalId = v;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String v) {
        discount = v;
    }

    public int getCostCoins() {
        return costCoins;
    }

    public void setCostCoins(int v) {
        costCoins = v;
    }
}
