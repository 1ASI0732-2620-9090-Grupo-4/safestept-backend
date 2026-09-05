package com.safestep.platform.gamification.application.internal.queryservices;

import com.safestep.platform.gamification.application.queryservices.GamificationQueryService;
import com.safestep.platform.gamification.domain.model.aggregates.*;
import com.safestep.platform.gamification.domain.model.queries.*;
import com.safestep.platform.gamification.domain.repositories.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GamificationQueryServiceImpl implements GamificationQueryService {
    private final PlayerProgressRepository progress;
    private final MissionRepository missions;
    private final BadgeRepository badges;
    private final CoinTransactionRepository transactions;
    private final PlayerAchievementRepository achievements;

    public GamificationQueryServiceImpl(PlayerProgressRepository p, MissionRepository m, BadgeRepository b,
            CoinTransactionRepository t, PlayerAchievementRepository a) {
        progress = p;
        missions = m;
        badges = b;
        transactions = t;
        achievements = a;
    }

    @Override
    public PlayerProgress handle(GetSummaryQuery query) {
        return progress.findByUsername(query.username())
                .orElseGet(() -> new PlayerProgress(null, query.username(), 1, 0, 0, 0, 0, null));
    }

    @Override
    public List<Mission> handle(GetMissionsQuery query) {
        return missions.findAll();
    }

    @Override
    public int handle(GetMissionProgressQuery query) {
        return achievements.missionProgress(query.username(), query.missionId());
    }

    @Override
    public List<Badge> handle(GetBadgesQuery query) {
        return badges.findAll();
    }

    @Override
    public Set<String> handle(GetUnlockedBadgeIdsQuery query) {
        return achievements.unlockedBadgeIds(query.username());
    }

    @Override
    public List<PlayerProgress> handle(GetLeaderboardQuery query) {
        return progress.findLeaderboard();
    }

    @Override
    public List<CoinTransaction> handle(GetCoinTransactionsQuery query) {
        return transactions.findByUsername(query.username());
    }
}
