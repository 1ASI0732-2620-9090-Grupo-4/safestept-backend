package com.safestep.platform.analytics.domain.model.valueobjects;

public record Metric(String label, String value, String trend) {
    public Metric {
        if (label == null || label.isBlank())
            throw new IllegalArgumentException("label must not be null or blank");
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("value must not be null or blank");
    }
}
