package com.safestep.platform.gamification.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "player_progress")
public class PlayerProgressPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true)
    private String username;
    private int level;
    private int xp;
    private int safeCoins;
    private int streakDays;
    private int completedSimulations;
    private LocalDate lastActivity;

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        username = v;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int v) {
        level = v;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int v) {
        xp = v;
    }

    public int getSafeCoins() {
        return safeCoins;
    }

    public void setSafeCoins(int v) {
        safeCoins = v;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(int v) {
        streakDays = v;
    }

    public int getCompletedSimulations() {
        return completedSimulations;
    }

    public void setCompletedSimulations(int v) {
        completedSimulations = v;
    }

    public LocalDate getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDate v) {
        lastActivity = v;
    }
}
