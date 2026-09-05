package com.safestep.platform.gamification.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "coin_transactions")
public class CoinTransactionPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true)
    private String externalId;
    @Column(nullable = false)
    private String username;
    private String simulationId;
    private String simulationTitle;
    private int baseCoins;
    private int earnedCoins;
    private int score;
    private double multiplier;
    private double accuracy;
    private Instant occurredAt;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String v) {
        externalId = v;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        username = v;
    }

    public String getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(String v) {
        simulationId = v;
    }

    public String getSimulationTitle() {
        return simulationTitle;
    }

    public void setSimulationTitle(String v) {
        simulationTitle = v;
    }

    public int getBaseCoins() {
        return baseCoins;
    }

    public void setBaseCoins(int v) {
        baseCoins = v;
    }

    public int getEarnedCoins() {
        return earnedCoins;
    }

    public void setEarnedCoins(int v) {
        earnedCoins = v;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int v) {
        score = v;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double v) {
        multiplier = v;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double v) {
        accuracy = v;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant v) {
        occurredAt = v;
    }
}
