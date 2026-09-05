package com.safestep.platform.analytics.domain.model.valueobjects;

public record SkillProgress(String skill, int progress) {
    public SkillProgress {
        if (skill == null || skill.isBlank())
            throw new IllegalArgumentException("skill must not be null or blank");
        if (progress < 0 || progress > 100)
            throw new IllegalArgumentException("progress must be between 0 and 100");
    }
}
