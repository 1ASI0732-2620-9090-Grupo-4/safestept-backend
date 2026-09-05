package com.safestep.platform.analytics.domain.model.valueobjects;

public record CommonMistake(String topic, int mistakes, String recommendation) {
    public CommonMistake {
        if (topic == null || topic.isBlank())
            throw new IllegalArgumentException("topic must not be null or blank");
        if (mistakes < 0)
            throw new IllegalArgumentException("mistakes must not be negative");
    }
}
