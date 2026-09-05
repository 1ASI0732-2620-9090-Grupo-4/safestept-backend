package com.safestep.platform.gamification.domain.model.commands;

import com.safestep.platform.gamification.domain.model.aggregates.Mission;

public record CreateMissionCommand(Mission mission) {
}
