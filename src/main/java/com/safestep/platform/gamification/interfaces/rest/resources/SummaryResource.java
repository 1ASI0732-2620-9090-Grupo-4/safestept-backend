package com.safestep.platform.gamification.interfaces.rest.resources;

public record SummaryResource(String username, int level, int xp, int safeCoins, int streak,
        int completedSimulations) {
}
