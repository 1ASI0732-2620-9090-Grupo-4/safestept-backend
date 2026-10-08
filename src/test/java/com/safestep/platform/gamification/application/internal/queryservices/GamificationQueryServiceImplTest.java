package com.safestep.platform.gamification.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.safestep.platform.gamification.domain.model.aggregates.Badge;
import com.safestep.platform.gamification.domain.model.aggregates.CoinTransaction;
import com.safestep.platform.gamification.domain.model.aggregates.Mission;
import com.safestep.platform.gamification.domain.model.aggregates.PlayerProgress;
import com.safestep.platform.gamification.domain.model.queries.GetBadgesQuery;
import com.safestep.platform.gamification.domain.model.queries.GetCoinTransactionsQuery;
import com.safestep.platform.gamification.domain.model.queries.GetLeaderboardQuery;
import com.safestep.platform.gamification.domain.model.queries.GetMissionProgressQuery;
import com.safestep.platform.gamification.domain.model.queries.GetMissionsQuery;
import com.safestep.platform.gamification.domain.model.queries.GetSummaryQuery;
import com.safestep.platform.gamification.domain.model.queries.GetUnlockedBadgeIdsQuery;
import com.safestep.platform.gamification.domain.repositories.BadgeRepository;
import com.safestep.platform.gamification.domain.repositories.CoinTransactionRepository;
import com.safestep.platform.gamification.domain.repositories.MissionRepository;
import com.safestep.platform.gamification.domain.repositories.PlayerAchievementRepository;
import com.safestep.platform.gamification.domain.repositories.PlayerProgressRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GamificationQueryServiceImplTest {

    private PlayerProgressRepository progress;
    private MissionRepository missions;
    private BadgeRepository badges;
    private CoinTransactionRepository transactions;
    private PlayerAchievementRepository achievements;
    private GamificationQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        progress = mock(PlayerProgressRepository.class);
        missions = mock(MissionRepository.class);
        badges = mock(BadgeRepository.class);
        transactions = mock(CoinTransactionRepository.class);
        achievements = mock(PlayerAchievementRepository.class);
        service = new GamificationQueryServiceImpl(progress, missions, badges, transactions, achievements);
    }

    @Test
    @DisplayName("handle(GetSummaryQuery) should return the stored progress when it exists (AAA)")
    void summary_ExistingPlayer_ReturnsStoredProgress() {
        // Arrange
        var stored = new PlayerProgress(1L, "ana", 3, 2400, 150, 4, 12, LocalDate.now());
        when(progress.findByUsername("ana")).thenReturn(Optional.of(stored));

        // Act
        var result = service.handle(new GetSummaryQuery("ana"));

        // Assert
        assertSame(stored, result);
    }

    @Test
    @DisplayName("handle(GetSummaryQuery) should return a level one default for unknown players (AAA)")
    void summary_UnknownPlayer_ReturnsDefaultProgress() {
        // Arrange
        when(progress.findByUsername("new")).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new GetSummaryQuery("new"));

        // Assert
        assertEquals("new", result.getUsername());
        assertEquals(1, result.getLevel());
        assertEquals(0, result.getSafeCoins());
    }

    @Test
    @DisplayName("list queries should delegate to their repositories (AAA)")
    void listQueries_DelegateToRepositories() {
        // Arrange
        var missionList = List.<Mission>of();
        var badgeList = List.<Badge>of();
        var leaderboard = List.<PlayerProgress>of();
        var coinTransactions = List.<CoinTransaction>of();
        when(missions.findAll()).thenReturn(missionList);
        when(badges.findAll()).thenReturn(badgeList);
        when(progress.findLeaderboard()).thenReturn(leaderboard);
        when(transactions.findByUsername("ana")).thenReturn(coinTransactions);

        // Act + Assert
        assertSame(missionList, service.handle(new GetMissionsQuery()));
        assertSame(badgeList, service.handle(new GetBadgesQuery("ana")));
        assertSame(leaderboard, service.handle(new GetLeaderboardQuery()));
        assertSame(coinTransactions, service.handle(new GetCoinTransactionsQuery("ana")));
    }

    @Test
    @DisplayName("achievement queries should delegate to the achievement repository (AAA)")
    void achievementQueries_DelegateToRepository() {
        // Arrange
        when(achievements.missionProgress("ana", "daily")).thenReturn(2);
        when(achievements.unlockedBadgeIds("ana")).thenReturn(Set.of("first-aid"));

        // Act + Assert
        assertEquals(2, service.handle(new GetMissionProgressQuery("ana", "daily")));
        assertEquals(Set.of("first-aid"), service.handle(new GetUnlockedBadgeIdsQuery("ana")));
    }
}
