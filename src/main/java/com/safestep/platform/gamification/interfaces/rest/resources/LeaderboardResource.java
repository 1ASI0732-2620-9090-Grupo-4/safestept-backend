package com.safestep.platform.gamification.interfaces.rest.resources;

public record LeaderboardResource(int rank, String name, int xp, int streak) {
}
