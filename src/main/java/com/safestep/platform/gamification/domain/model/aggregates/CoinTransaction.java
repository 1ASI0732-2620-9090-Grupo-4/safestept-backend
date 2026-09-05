package com.safestep.platform.gamification.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.time.Instant;

public class CoinTransaction extends AbstractDomainAggregateRoot<CoinTransaction> {
    private Long id;
    private final String externalId, username, simulationId, simulationTitle;
    private final int baseCoins, earnedCoins, score;
    private final double multiplier, accuracy;
    private final Instant createdAt;

    public CoinTransaction(Long id, String externalId, String username, String simulationId, String title, int base,
            int earned, int score, double multiplier, double accuracy, Instant at) {
        this.id = id;
        this.externalId = externalId;
        this.username = username;
        this.simulationId = simulationId;
        simulationTitle = title;
        baseCoins = base;
        earnedCoins = earned;
        this.score = score;
        this.multiplier = multiplier;
        this.accuracy = accuracy;
        createdAt = at;
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

    public String getUsername() {
        return username;
    }

    public String getSimulationId() {
        return simulationId;
    }

    public String getSimulationTitle() {
        return simulationTitle;
    }

    public int getBaseCoins() {
        return baseCoins;
    }

    public int getEarnedCoins() {
        return earnedCoins;
    }

    public int getScore() {
        return score;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
