package com.safestep.platform.simulation.domain.model.events;

import java.time.Instant;

public record SimulationAttemptCompletedEvent(String attemptId, String username, String simulationSlug,
        String simulationTitle, int score, double accuracy, int xpReward, int coinReward, Instant occurredOn) {
}
