package com.safestep.platform.gamification.domain.repositories;

import java.util.*;

public interface PlayerAchievementRepository {
    void advanceMission(String username, String missionId, int goal);

    int missionProgress(String username, String missionId);

    void unlockBadge(String username, String badgeId);

    Set<String> unlockedBadgeIds(String username);
}
