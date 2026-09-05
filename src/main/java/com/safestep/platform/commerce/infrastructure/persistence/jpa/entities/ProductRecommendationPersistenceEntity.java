package com.safestep.platform.commerce.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_recommendations")
public class ProductRecommendationPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(unique = true, nullable = false)
    private String externalId;
    private String username, productId, reason, relatedSimulationId, priority;
    private boolean dismissed;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String v) {
        reason = v;
    }

    public String getRelatedSimulationId() {
        return relatedSimulationId;
    }

    public void setRelatedSimulationId(String v) {
        relatedSimulationId = v;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String v) {
        priority = v;
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean v) {
        dismissed = v;
    }
}
