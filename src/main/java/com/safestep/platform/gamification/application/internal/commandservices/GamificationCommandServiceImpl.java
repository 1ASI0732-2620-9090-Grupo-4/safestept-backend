package com.safestep.platform.gamification.application.internal.commandservices;

import com.safestep.platform.gamification.application.commandservices.GamificationCommandService;
import com.safestep.platform.gamification.domain.model.aggregates.Badge;
import com.safestep.platform.gamification.domain.model.aggregates.Mission;
import com.safestep.platform.gamification.domain.model.commands.CreateBadgeCommand;
import com.safestep.platform.gamification.domain.model.commands.CreateMissionCommand;
import com.safestep.platform.gamification.domain.model.commands.DeleteBadgeCommand;
import com.safestep.platform.gamification.domain.model.commands.DeleteMissionCommand;
import com.safestep.platform.gamification.domain.model.commands.UpdateBadgeCommand;
import com.safestep.platform.gamification.domain.model.commands.UpdateMissionCommand;
import com.safestep.platform.gamification.domain.repositories.BadgeRepository;
import com.safestep.platform.gamification.domain.repositories.MissionRepository;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class GamificationCommandServiceImpl implements GamificationCommandService {
    private final MissionRepository missions;
    private final BadgeRepository badges;

    public GamificationCommandServiceImpl(MissionRepository missions, BadgeRepository badges) {
        this.missions = missions;
        this.badges = badges;
    }

    @Override
    public Result<Mission, ApplicationError> handle(CreateMissionCommand command) {
        var mission = command.mission();
        if (mission.getExternalId() == null || mission.getExternalId().isBlank())
            return Result.failure(ApplicationError.validationError("mission", "Mission id is required"));
        var validation = validateMission(mission);
        if (validation != null)
            return Result.failure(validation);
        if (missions.existsByExternalId(mission.getExternalId()))
            return Result.failure(ApplicationError.conflict("mission", "Mission id already exists"));
        return Result.success(missions.save(mission));
    }

    @Override
    public Result<Mission, ApplicationError> handle(UpdateMissionCommand command) {
        if (command.missionId() == null || command.missionId().isBlank())
            return Result.failure(ApplicationError.validationError("mission", "Mission id is required"));
        var found = missions.findByExternalId(command.missionId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("mission", command.missionId()));
        var source = command.mission();
        var validation = validateMission(source);
        if (validation != null)
            return Result.failure(validation);
        var mission = new Mission(found.get().getId(), command.missionId(), source.getTitle(), source.getCadence(),
                source.getGoal(), source.getRewardXp(), source.getRewardCoins(), source.getStatus(),
                source.getInstructions(), source.getUnlockRequirement());
        return Result.success(missions.save(mission));
    }

    @Override
    public Result<String, ApplicationError> handle(DeleteMissionCommand command) {
        var found = missions.findByExternalId(command.missionId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("mission", command.missionId()));
        missions.delete(found.get());
        return Result.success("Mission deleted");
    }

    @Override
    public Result<Badge, ApplicationError> handle(CreateBadgeCommand command) {
        var badge = command.badge();
        if (badge.getExternalId() == null || badge.getExternalId().isBlank())
            return Result.failure(ApplicationError.validationError("badge", "Badge id is required"));
        if (badges.existsByExternalId(badge.getExternalId()))
            return Result.failure(ApplicationError.conflict("badge", "Badge id already exists"));
        return Result.success(badges.save(badge));
    }

    @Override
    public Result<Badge, ApplicationError> handle(UpdateBadgeCommand command) {
        if (command.badgeId() == null || command.badgeId().isBlank())
            return Result.failure(ApplicationError.validationError("badge", "Badge id is required"));
        var found = badges.findByExternalId(command.badgeId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("badge", command.badgeId()));
        var source = command.badge();
        var badge = new Badge(found.get().getId(), command.badgeId(), source.getName(), source.getRarity(),
                source.getDescription(), source.getUnlockRequirement());
        return Result.success(badges.save(badge));
    }

    @Override
    public Result<String, ApplicationError> handle(DeleteBadgeCommand command) {
        var found = badges.findByExternalId(command.badgeId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("badge", command.badgeId()));
        badges.delete(found.get());
        return Result.success("Badge deleted");
    }

    private ApplicationError validateMission(Mission mission) {
        if (mission.getGoal() < 0)
            return ApplicationError.validationError("mission", "Goal cannot be negative");
        if (mission.getRewardXp() < 0)
            return ApplicationError.validationError("mission", "XP reward cannot be negative");
        if (mission.getRewardCoins() < 0)
            return ApplicationError.validationError("mission", "Coin reward cannot be negative");
        return null;
    }
}
