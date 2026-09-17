package com.safestep.platform.gamification.interfaces.acl;

public interface GamificationContextFacade {
    ProgressSnapshot progressByUsername(String username);

    boolean spendCoins(String username, int amount, String couponId, String couponTitle);

    record ProgressSnapshot(int level, int xp, int safeCoins, int streak, int completedSimulations) {
    }
}
