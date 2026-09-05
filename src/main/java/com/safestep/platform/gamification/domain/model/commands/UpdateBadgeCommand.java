package com.safestep.platform.gamification.domain.model.commands;

import com.safestep.platform.gamification.domain.model.aggregates.Badge;

public record UpdateBadgeCommand(String badgeId, Badge badge) {
}
