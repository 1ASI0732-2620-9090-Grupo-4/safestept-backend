package com.safestep.platform.gamification.interfaces.rest.resources;

public record BadgeResource(String id, String name, String rarity, boolean unlocked, String description,
        String unlockRequirement) {
}
