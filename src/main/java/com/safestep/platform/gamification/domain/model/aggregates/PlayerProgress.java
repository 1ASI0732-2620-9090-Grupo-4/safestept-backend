package com.safestep.platform.gamification.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.time.*;

public class PlayerProgress extends AbstractDomainAggregateRoot<PlayerProgress> {
    private Long id;
    private final String username;
    private int level;
    private int xp;
    private int safeCoins;
    private int streakDays;
    private int completedSimulations;
    private LocalDate lastActivity;

    public PlayerProgress(Long id, String username, int level, int xp, int coins, int streak, int completed,
            LocalDate last) {
        this.id = id;
        this.username = username;
        this.level = Math.max(1, level);
        this.xp = Math.max(0, xp);
        safeCoins = Math.max(0, coins);
        streakDays = Math.max(0, streak);
        completedSimulations = Math.max(0, completed);
        lastActivity = last;
    }

    public void reward(int earnedXp, int coins) {
        xp += Math.max(0, earnedXp);
        safeCoins += Math.max(0, coins);
        completedSimulations++;
        var today = LocalDate.now();
        if (lastActivity != null && lastActivity.plusDays(1).equals(today))
            streakDays++;
        else if (!today.equals(lastActivity))
            streakDays = 1;
        lastActivity = today;
        level = 1 + (xp / 1000);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        id = v;
    }

    public String getUsername() {
        return username;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getSafeCoins() {
        return safeCoins;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public int getCompletedSimulations() {
        return completedSimulations;
    }

    public LocalDate getLastActivity() {
        return lastActivity;
    }
}
