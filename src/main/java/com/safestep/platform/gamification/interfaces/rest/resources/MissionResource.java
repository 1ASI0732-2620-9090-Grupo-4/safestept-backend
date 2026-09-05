package com.safestep.platform.gamification.interfaces.rest.resources;

public record MissionResource(String id, String title, String cadence, int progress, int goal, int rewardXp,
        int rewardCoins, String status, String instructions, String unlockRequirement) {
}
