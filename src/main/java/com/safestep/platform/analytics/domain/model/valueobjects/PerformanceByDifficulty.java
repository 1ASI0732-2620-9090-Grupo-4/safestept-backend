package com.safestep.platform.analytics.domain.model.valueobjects;

public record PerformanceByDifficulty(String difficulty, int completed, int accuracy) {
    public PerformanceByDifficulty {
        if (difficulty == null || difficulty.isBlank())
            throw new IllegalArgumentException("difficulty must not be null or blank");
        if (completed < 0)
            throw new IllegalArgumentException("completed must not be negative");
        if (accuracy < 0 || accuracy > 100)
            throw new IllegalArgumentException("accuracy must be between 0 and 100");
    }
}
