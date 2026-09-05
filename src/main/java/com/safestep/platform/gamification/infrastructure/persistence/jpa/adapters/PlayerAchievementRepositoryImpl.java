package com.safestep.platform.gamification.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.gamification.domain.repositories.PlayerAchievementRepository;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.entities.*;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories.*;
import org.springframework.stereotype.Repository;
import java.time.*;
import java.util.*;

@Repository
public class PlayerAchievementRepositoryImpl implements PlayerAchievementRepository {
    private final UserMissionProgressPersistenceRepository missions;
    private final UserBadgePersistenceRepository badges;

    public PlayerAchievementRepositoryImpl(UserMissionProgressPersistenceRepository m,
            UserBadgePersistenceRepository b) {
        missions = m;
        badges = b;
    }

    public void advanceMission(String u, String id, int goal) {
        var e = missions.findByUsernameAndMissionExternalId(u, id).orElseGet(() -> {
            var value = new UserMissionProgressPersistenceEntity();
            value.setUsername(u);
            value.setMissionExternalId(id);
            return value;
        });
        e.setProgress(Math.min(goal, e.getProgress() + 1));
        e.setCompleted(e.getProgress() >= goal);
        missions.save(e);
    }

    public int missionProgress(String u, String id) {
        return missions.findByUsernameAndMissionExternalId(u, id).map(UserMissionProgressPersistenceEntity::getProgress)
                .orElse(0);
    }

    public void unlockBadge(String u, String id) {
        if (badges.existsByUsernameAndBadgeExternalId(u, id))
            return;
        var e = new UserBadgePersistenceEntity();
        e.setUsername(u);
        e.setBadgeExternalId(id);
        e.setUnlockedAt(Instant.now());
        badges.save(e);
    }

    public Set<String> unlockedBadgeIds(String u) {
        var ids = new HashSet<String>();
        badges.findByUsername(u).forEach(e -> ids.add(e.getBadgeExternalId()));
        return ids;
    }
}
