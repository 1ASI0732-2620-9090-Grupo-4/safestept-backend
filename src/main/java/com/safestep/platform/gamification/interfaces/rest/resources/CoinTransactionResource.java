package com.safestep.platform.gamification.interfaces.rest.resources;

import java.time.Instant;

public record CoinTransactionResource(String id, String userId, String simulationId, String simulationTitle,
        int baseCoins, double multiplier, double accuracy, int earnedCoins, boolean successful, Instant createdAt) {
}
