package com.safestep.platform.commerce.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class CategoryPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(unique = true, nullable = false)
    private String externalId;
    private String name;
    private int productCount;

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

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int v) {
        productCount = v;
    }
}
