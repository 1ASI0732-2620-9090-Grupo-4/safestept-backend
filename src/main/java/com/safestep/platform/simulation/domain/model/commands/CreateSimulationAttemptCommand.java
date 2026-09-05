package com.safestep.platform.simulation.domain.model.commands;

import com.safestep.platform.simulation.domain.model.entities.AttemptError;
import java.time.Instant;
import java.util.List;

public record CreateSimulationAttemptCommand(String simulationSlug, String username, String mode, Instant startedAt,
        Instant completedAt, int score, int totalSteps, int correctSteps, long timeElapsed,
        List<AttemptError> errors) {
}
