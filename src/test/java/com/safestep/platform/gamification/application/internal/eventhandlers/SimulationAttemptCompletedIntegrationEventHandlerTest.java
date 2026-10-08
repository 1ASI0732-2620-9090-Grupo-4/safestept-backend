package com.safestep.platform.gamification.application.internal.eventhandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.safestep.platform.gamification.domain.model.aggregates.Badge;
import com.safestep.platform.gamification.domain.model.aggregates.CoinTransaction;
import com.safestep.platform.gamification.domain.model.aggregates.Mission;
import com.safestep.platform.gamification.domain.model.aggregates.PlayerProgress;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.BadgeRarity;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.MissionCadence;
import com.safestep.platform.gamification.domain.repositories.BadgeRepository;
import com.safestep.platform.gamification.domain.repositories.CoinTransactionRepository;
import com.safestep.platform.gamification.domain.repositories.MissionRepository;
import com.safestep.platform.gamification.domain.repositories.PlayerAchievementRepository;
import com.safestep.platform.gamification.domain.repositories.PlayerProgressRepository;
import com.safestep.platform.simulation.interfaces.events.SimulationAttemptCompletedIntegrationEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SimulationAttemptCompletedIntegrationEventHandlerTest {

    private PlayerProgressRepository progress;
    private CoinTransactionRepository transactions;
    private PlayerAchievementRepository achievements;
    private MissionRepository missions;
    private BadgeRepository badges;
    private SimulationAttemptCompletedIntegrationEventHandler handler;

    private final SimulationAttemptCompletedIntegrationEvent event = new SimulationAttemptCompletedIntegrationEvent(
            "att-1", "ana", "cpr-adult", "Adult CPR", 90, 0.9, 120, 30, Instant.parse("2026-09-14T15:00:00Z"));

    @BeforeEach
    void setUp() {
        progress = mock(PlayerProgressRepository.class);
        transactions = mock(CoinTransactionRepository.class);
        achievements = mock(PlayerAchievementRepository.class);
        missions = mock(MissionRepository.class);
        badges = mock(BadgeRepository.class);
        handler = new SimulationAttemptCompletedIntegrationEventHandler(progress, transactions, achievements,
                missions, badges);
    }

    @Test
    @DisplayName("on should be idempotent for an attempt that was already rewarded (AAA)")
    void on_AlreadyProcessedAttempt_DoesNothing() {
        // Arrange
        when(transactions.existsByExternalId("attempt-att-1")).thenReturn(true);

        // Act
        handler.on(event);

        // Assert
        verify(progress, never()).save(any());
        verify(transactions, never()).save(any());
    }

    @Test
    @DisplayName("on should reward a brand new player, record the transaction and unlock the first badge (AAA)")
    void on_NewPlayer_RewardsAndUnlocks() {
        // Arrange
        when(transactions.existsByExternalId("attempt-att-1")).thenReturn(false);
        when(progress.findByUsername("ana")).thenReturn(Optional.empty());
        when(missions.findAll()).thenReturn(List.of(
                new Mission(1L, "weekly", "Weekly", MissionCadence.WEEKLY, 5, 10, 10, "ACTIVE", "i", "u"),
                new Mission(2L, "daily", "Daily", MissionCadence.DAILY, 3, 10, 10, "ACTIVE", "i", "u")));
        when(badges.findAll()).thenReturn(List.of(
                new Badge(1L, "legend", "Legend", BadgeRarity.LEGENDARY, "d", "r"),
                new Badge(2L, "first-aid", "First", BadgeRarity.COMMON, "d", "r")));

        // Act
        handler.on(event);

        // Assert
        var savedProgress = ArgumentCaptor.forClass(PlayerProgress.class);
        verify(progress).save(savedProgress.capture());
        assertEquals(30, savedProgress.getValue().getSafeCoins());
        assertEquals(120, savedProgress.getValue().getXp());
        var savedTransaction = ArgumentCaptor.forClass(CoinTransaction.class);
        verify(transactions).save(savedTransaction.capture());
        assertEquals("attempt-att-1", savedTransaction.getValue().getExternalId());
        assertEquals(30, savedTransaction.getValue().getEarnedCoins());
        verify(achievements).advanceMission("ana", "daily", 3);
        verify(achievements).unlockBadge("ana", "first-aid");
    }

    @Test
    @DisplayName("on should add the reward to the existing player progress (AAA)")
    void on_ExistingPlayer_AddsReward() {
        // Arrange
        var existing = new PlayerProgress(9L, "ana", 1, 500, 100, 2, 4, null);
        when(transactions.existsByExternalId("attempt-att-1")).thenReturn(false);
        when(progress.findByUsername("ana")).thenReturn(Optional.of(existing));
        when(missions.findAll()).thenReturn(List.of());
        when(badges.findAll()).thenReturn(List.of());

        // Act
        handler.on(event);

        // Assert
        assertEquals(130, existing.getSafeCoins());
        assertEquals(620, existing.getXp());
        assertEquals(5, existing.getCompletedSimulations());
        verify(achievements, never()).advanceMission(anyString(), anyString(), anyInt());
        verify(achievements, never()).unlockBadge(anyString(), anyString());
    }
}
