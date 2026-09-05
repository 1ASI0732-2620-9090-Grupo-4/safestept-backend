package com.safestep.platform.analytics.domain.model.valueobjects;

import java.time.Instant;
import java.util.List;

public record ProgressVisual(String simulationId, int completionPercentage, Integer bestScore, Double averageScore,
        int totalAttempts, String statusColor, Instant lastPracticedAt, List<String> commonErrors,
        Long averageResponseTime) {
    public ProgressVisual {
        if (simulationId == null || simulationId.isBlank())
            throw new IllegalArgumentException("simulationId must not be null or blank");
        if (totalAttempts < 0)
            throw new IllegalArgumentException("totalAttempts must not be negative");
    }
}
