package com.safestep.platform.gamification.application.commandservices;

import com.safestep.platform.gamification.domain.model.aggregates.Badge;
import com.safestep.platform.gamification.domain.model.aggregates.Mission;
import com.safestep.platform.gamification.domain.model.commands.CreateBadgeCommand;
import com.safestep.platform.gamification.domain.model.commands.CreateMissionCommand;
import com.safestep.platform.gamification.domain.model.commands.DeleteBadgeCommand;
import com.safestep.platform.gamification.domain.model.commands.DeleteMissionCommand;
import com.safestep.platform.gamification.domain.model.commands.UpdateBadgeCommand;
import com.safestep.platform.gamification.domain.model.commands.UpdateMissionCommand;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;

public interface GamificationCommandService {
    Result<Mission, ApplicationError> handle(CreateMissionCommand command);

    Result<Mission, ApplicationError> handle(UpdateMissionCommand command);

    Result<String, ApplicationError> handle(DeleteMissionCommand command);

    Result<Badge, ApplicationError> handle(CreateBadgeCommand command);

    Result<Badge, ApplicationError> handle(UpdateBadgeCommand command);

    Result<String, ApplicationError> handle(DeleteBadgeCommand command);
}
