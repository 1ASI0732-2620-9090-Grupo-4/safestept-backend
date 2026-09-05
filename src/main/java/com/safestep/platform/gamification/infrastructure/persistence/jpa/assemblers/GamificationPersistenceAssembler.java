package com.safestep.platform.gamification.infrastructure.persistence.jpa.assemblers;

import com.safestep.platform.gamification.domain.model.aggregates.*;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.*;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.entities.*;

public final class GamificationPersistenceAssembler {
    private GamificationPersistenceAssembler() {
    }

    public static PlayerProgress toDomain(PlayerProgressPersistenceEntity e) {
        return new PlayerProgress(e.getId(), e.getUsername(), e.getLevel(), e.getXp(), e.getSafeCoins(),
                e.getStreakDays(), e.getCompletedSimulations(), e.getLastActivity());
    }

    public static PlayerProgressPersistenceEntity toEntity(PlayerProgress d) {
        var e = new PlayerProgressPersistenceEntity();
        e.setId(d.getId());
        e.setUsername(d.getUsername());
        e.setLevel(d.getLevel());
        e.setXp(d.getXp());
        e.setSafeCoins(d.getSafeCoins());
        e.setStreakDays(d.getStreakDays());
        e.setCompletedSimulations(d.getCompletedSimulations());
        e.setLastActivity(d.getLastActivity());
        return e;
    }

    public static Mission toDomain(MissionPersistenceEntity e) {
        return new Mission(e.getId(), e.getExternalId(), e.getTitle(), MissionCadence.from(e.getCadence()), e.getGoal(),
                e.getRewardXp(), e.getRewardCoins(), e.getStatus(), e.getInstructions(), e.getUnlockRequirement());
    }

    public static MissionPersistenceEntity toEntity(Mission d) {
        var e = new MissionPersistenceEntity();
        e.setId(d.getId());
        e.setExternalId(d.getExternalId());
        e.setTitle(d.getTitle());
        e.setCadence(d.getCadence().name());
        e.setGoal(d.getGoal());
        e.setRewardXp(d.getRewardXp());
        e.setRewardCoins(d.getRewardCoins());
        e.setStatus(d.getStatus());
        e.setInstructions(d.getInstructions());
        e.setUnlockRequirement(d.getUnlockRequirement());
        return e;
    }

    public static Badge toDomain(BadgePersistenceEntity e) {
        return new Badge(e.getId(), e.getExternalId(), e.getName(), BadgeRarity.from(e.getRarity()), e.getDescription(),
                e.getUnlockRequirement());
    }

    public static BadgePersistenceEntity toEntity(Badge d) {
        var e = new BadgePersistenceEntity();
        e.setId(d.getId());
        e.setExternalId(d.getExternalId());
        e.setName(d.getName());
        e.setRarity(d.getRarity().name());
        e.setDescription(d.getDescription());
        e.setUnlockRequirement(d.getUnlockRequirement());
        return e;
    }

    public static CoinTransaction toDomain(CoinTransactionPersistenceEntity e) {
        return new CoinTransaction(e.getId(), e.getExternalId(), e.getUsername(), e.getSimulationId(),
                e.getSimulationTitle(), e.getBaseCoins(), e.getEarnedCoins(), e.getScore(), e.getMultiplier(),
                e.getAccuracy(), e.getOccurredAt());
    }

    public static CoinTransactionPersistenceEntity toEntity(CoinTransaction d) {
        var e = new CoinTransactionPersistenceEntity();
        e.setId(d.getId());
        e.setExternalId(d.getExternalId());
        e.setUsername(d.getUsername());
        e.setSimulationId(d.getSimulationId());
        e.setSimulationTitle(d.getSimulationTitle());
        e.setBaseCoins(d.getBaseCoins());
        e.setEarnedCoins(d.getEarnedCoins());
        e.setScore(d.getScore());
        e.setMultiplier(d.getMultiplier());
        e.setAccuracy(d.getAccuracy());
        e.setOccurredAt(d.getCreatedAt());
        return e;
    }
}
