package com.safestep.platform.gamification.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.BadgeRarity;

public class Badge extends AbstractDomainAggregateRoot<Badge> {
    private Long id;
    private final String externalId, name, description, unlockRequirement;
    private final BadgeRarity rarity;

    public Badge(Long id, String externalId, String name, BadgeRarity rarity, String description, String requirement) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.rarity = rarity;
        this.description = description;
        unlockRequirement = requirement;
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

    public String getName() {
        return name;
    }

    public BadgeRarity getRarity() {
        return rarity;
    }

    public String getDescription() {
        return description;
    }

    public String getUnlockRequirement() {
        return unlockRequirement;
    }
}
