package com.safestep.platform.gamification.interfaces.acl;

public interface GamificationContextFacade {
    ProgressSnapshot progressByUsername(String username);

    record ProgressSnapshot(int level, int xp, int safeCoins, int streak, int completedSimulations) {
    }
}
