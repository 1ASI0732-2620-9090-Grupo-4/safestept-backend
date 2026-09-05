package com.safestep.platform.gamification.application.queryservices;

import com.safestep.platform.gamification.domain.model.aggregates.*;
import com.safestep.platform.gamification.domain.model.queries.*;
import java.util.*;

public interface GamificationQueryService {
    PlayerProgress handle(GetSummaryQuery query);
    List<Mission> handle(GetMissionsQuery query);
    int handle(GetMissionProgressQuery query);
    List<Badge> handle(GetBadgesQuery query);
    Set<String> handle(GetUnlockedBadgeIdsQuery query);
    List<PlayerProgress> handle(GetLeaderboardQuery query);
    List<CoinTransaction> handle(GetCoinTransactionsQuery query);
}
