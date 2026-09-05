package com.safestep.platform.gamification.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.MissionCadence;

public class Mission extends AbstractDomainAggregateRoot<Mission> {
    private Long id;
    private final String externalId, title;
    private final MissionCadence cadence;
    private final int goal, rewardXp, rewardCoins;
    private final String status, instructions, unlockRequirement;

    public Mission(Long id, String externalId, String title, MissionCadence cadence, int goal, int rewardXp,
            int rewardCoins, String status, String instructions, String unlockRequirement) {
        this.id = id;
        this.externalId = externalId;
        this.title = title;
        this.cadence = cadence;
        this.goal = goal;
        this.rewardXp = rewardXp;
        this.rewardCoins = rewardCoins;
        this.status = status;
        this.instructions = instructions;
        this.unlockRequirement = unlockRequirement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        id = v;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public MissionCadence getCadence() {
        return cadence;
    }

    public int getGoal() {
        return goal;
    }

    public int getRewardXp() {
        return rewardXp;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public String getStatus() {
        return status;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getUnlockRequirement() {
        return unlockRequirement;
    }
}
