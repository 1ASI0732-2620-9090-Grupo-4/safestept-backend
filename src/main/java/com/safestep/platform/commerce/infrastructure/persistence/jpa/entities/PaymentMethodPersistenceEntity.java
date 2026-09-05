package com.safestep.platform.commerce.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.math.*;

@Entity
@Table(name = "payment_methods")
public class PaymentMethodPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(unique = true, nullable = false)
    private String externalId;
    private String type, label, description;
    private BigDecimal processingFee;
    private boolean available;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String v) {
        externalId = v;
    }

    public String getType() {
        return type;
    }

    public void setType(String v) {
        type = v;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String v) {
        label = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal v) {
        processingFee = v;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean v) {
        available = v;
    }
}
