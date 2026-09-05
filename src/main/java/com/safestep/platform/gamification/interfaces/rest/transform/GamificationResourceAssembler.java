package com.safestep.platform.gamification.interfaces.rest.transform;

import com.safestep.platform.gamification.domain.model.aggregates.*;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.BadgeRarity;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.MissionCadence;
import com.safestep.platform.gamification.interfaces.rest.resources.*;

public final class GamificationResourceAssembler {
    private GamificationResourceAssembler() {
    }

    public static SummaryResource toResource(PlayerProgress p) {
        return new SummaryResource(p.getUsername(), p.getLevel(), p.getXp(), p.getSafeCoins(), p.getStreakDays(),
                p.getCompletedSimulations());
    }

    public static MissionResource toResource(Mission m, int progress) {
        return new MissionResource(m.getExternalId(), m.getTitle(), m.getCadence().name(), progress, m.getGoal(),
                m.getRewardXp(), m.getRewardCoins(), m.getStatus(), m.getInstructions(), m.getUnlockRequirement());
    }

    public static Mission toMission(MissionResource r) {
        return new Mission(null, r.id(), r.title(), MissionCadence.from(r.cadence()), r.goal(), r.rewardXp(),
                r.rewardCoins(), r.status(), r.instructions(), r.unlockRequirement());
    }

    public static BadgeResource toResource(Badge b, boolean unlocked) {
        return new BadgeResource(b.getExternalId(), b.getName(), b.getRarity().name(), unlocked, b.getDescription(),
                b.getUnlockRequirement());
    }

    public static Badge toBadge(BadgeResource r) {
        return new Badge(null, r.id(), r.name(), BadgeRarity.from(r.rarity()), r.description(),
                r.unlockRequirement());
    }

    public static LeaderboardResource toResource(PlayerProgress p, int rank) {
        return new LeaderboardResource(rank, p.getUsername(), p.getXp(), p.getStreakDays());
    }

    public static CoinTransactionResource toResource(CoinTransaction t) {
        return new CoinTransactionResource(t.getExternalId(), t.getUsername(), t.getSimulationId(),
                t.getSimulationTitle(), t.getBaseCoins(), t.getMultiplier(), t.getAccuracy(), t.getEarnedCoins(), true,
                t.getCreatedAt());
    }
}
