package com.safestep.platform.gamification.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "badges")
public class BadgePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true)
    private String externalId;
    @Column(nullable = false)
    private String name;
    private String rarity;
    @Column(length = 1000)
    private String description;
    @Column(length = 1000)
    private String unlockRequirement;

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

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String v) {
        rarity = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public String getUnlockRequirement() {
        return unlockRequirement;
    }

    public void setUnlockRequirement(String v) {
        unlockRequirement = v;
    }
}
