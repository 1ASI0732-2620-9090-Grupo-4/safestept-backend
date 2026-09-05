package com.safestep.platform.gamification.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "missions")
public class MissionPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true)
    private String externalId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String cadence;
    private int goal;
    private int rewardXp;
    private int rewardCoins;
    private String status;
    @Column(length = 1000)
    private String instructions;
    @Column(length = 1000)
    private String unlockRequirement;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String v) {
        externalId = v;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getCadence() {
        return cadence;
    }

    public void setCadence(String v) {
        cadence = v;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int v) {
        goal = v;
    }

    public int getRewardXp() {
        return rewardXp;
    }

    public void setRewardXp(int v) {
        rewardXp = v;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(int v) {
        rewardCoins = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String v) {
        instructions = v;
    }

    public String getUnlockRequirement() {
        return unlockRequirement;
    }

    public void setUnlockRequirement(String v) {
        unlockRequirement = v;
    }
}
