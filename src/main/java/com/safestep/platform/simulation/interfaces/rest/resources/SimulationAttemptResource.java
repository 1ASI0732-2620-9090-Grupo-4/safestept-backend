package com.safestep.platform.simulation.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record SimulationAttemptResource(String id, String userId, String scenarioId, String mode, Instant startedAt,
        Instant completedAt, int score, int totalSteps, int correctSteps, long timeElapsed,
        List<AttemptErrorResource> errors, String scenarioSlug) {
}
