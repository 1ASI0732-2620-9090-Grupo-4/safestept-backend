package com.safestep.platform.simulation.interfaces.events;

import java.time.Instant;

public record SimulationAttemptCompletedIntegrationEvent(String attemptId, String username, String simulationSlug,
        String simulationTitle, int score, double accuracy, int xpReward, int coinReward, Instant occurredOn) {
}
