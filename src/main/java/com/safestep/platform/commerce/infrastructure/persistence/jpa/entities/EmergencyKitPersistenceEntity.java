package com.safestep.platform.commerce.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.math.*;

@Entity
@Table(name = "emergency_kits")
public class EmergencyKitPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(unique = true, nullable = false)
    private String externalId;
    private String name, level, imageUrl;
    @Column(length = 2000)
    private String description;
    private BigDecimal individualPrice, kitPrice, savings;
    private int savingsPercentage;
    private boolean popular;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String v) {
        externalId = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String v) {
        level = v;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String v) {
        imageUrl = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public BigDecimal getIndividualPrice() {
        return individualPrice;
    }

    public void setIndividualPrice(BigDecimal v) {
        individualPrice = v;
    }

    public BigDecimal getKitPrice() {
        return kitPrice;
    }

    public void setKitPrice(BigDecimal v) {
        kitPrice = v;
    }

    public BigDecimal getSavings() {
        return savings;
    }

    public void setSavings(BigDecimal v) {
        savings = v;
    }

    public int getSavingsPercentage() {
        return savingsPercentage;
    }

    public void setSavingsPercentage(int v) {
        savingsPercentage = v;
    }

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean v) {
        popular = v;
    }
}
