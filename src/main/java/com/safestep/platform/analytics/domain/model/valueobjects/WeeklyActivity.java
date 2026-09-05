package com.safestep.platform.analytics.domain.model.valueobjects;

public record WeeklyActivity(String day, int xp, int simulations) {
    public WeeklyActivity {
        if (day == null || day.isBlank())
            throw new IllegalArgumentException("day must not be null or blank");
        if (xp < 0)
            throw new IllegalArgumentException("xp must not be negative");
        if (simulations < 0)
            throw new IllegalArgumentException("simulations must not be negative");
    }
}
