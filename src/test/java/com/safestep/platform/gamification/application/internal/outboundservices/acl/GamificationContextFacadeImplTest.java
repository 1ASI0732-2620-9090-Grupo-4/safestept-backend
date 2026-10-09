package com.safestep.platform.gamification.application.internal.outboundservices.acl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.safestep.platform.gamification.domain.model.aggregates.CoinSpend;
import com.safestep.platform.gamification.domain.model.aggregates.PlayerProgress;
import com.safestep.platform.gamification.domain.repositories.CoinSpendRepository;
import com.safestep.platform.gamification.domain.repositories.PlayerProgressRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GamificationContextFacadeImplTest {

    private PlayerProgressRepository progress;
    private CoinSpendRepository coinSpends;
    private GamificationContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        progress = mock(PlayerProgressRepository.class);
        coinSpends = mock(CoinSpendRepository.class);
        facade = new GamificationContextFacadeImpl(progress, coinSpends);
    }

    @Test
    @DisplayName("progressByUsername should expose the stored progress as a snapshot (AAA)")
    void progressByUsername_ExistingPlayer() {
        // Arrange
        when(progress.findByUsername("ana"))
                .thenReturn(Optional.of(new PlayerProgress(1L, "ana", 3, 2400, 150, 4, 12, LocalDate.now())));

        // Act
        var snapshot = facade.progressByUsername("ana");

        // Assert
        assertEquals(3, snapshot.level());
        assertEquals(2400, snapshot.xp());
        assertEquals(150, snapshot.safeCoins());
        assertEquals(4, snapshot.streak());
        assertEquals(12, snapshot.completedSimulations());
    }

    @Test
    @DisplayName("progressByUsername should default to level one for unknown players (AAA)")
    void progressByUsername_UnknownPlayer() {
        // Arrange
        when(progress.findByUsername("new")).thenReturn(Optional.empty());

        // Act
        var snapshot = facade.progressByUsername("new");

        // Assert
        assertEquals(1, snapshot.level());
        assertEquals(0, snapshot.safeCoins());
    }

    @Test
    @DisplayName("spendCoins should debit the balance and record the spend when there are enough coins (AAA)")
    void spendCoins_WithEnoughCoins_DebitsAndRecords() {
        // Arrange
        var player = new PlayerProgress(1L, "ana", 1, 0, 500, 0, 0, LocalDate.now());
        when(progress.findByUsername("ana")).thenReturn(Optional.of(player));

        // Act
        var spent = facade.spendCoins("ana", 150, "cpn-5", "5% off");

        // Assert
        assertTrue(spent);
        assertEquals(350, player.getSafeCoins());
        verify(progress).save(player);
        var recorded = ArgumentCaptor.forClass(CoinSpend.class);
        verify(coinSpends).save(recorded.capture());
        assertEquals("ana", recorded.getValue().getUsername());
        assertEquals(150, recorded.getValue().getAmount());
        assertEquals("cpn-5", recorded.getValue().getCouponId());
    }

    @Test
    @DisplayName("spendCoins should refuse and persist nothing when the balance is insufficient (AAA)")
    void spendCoins_WithInsufficientCoins_ReturnsFalse() {
        // Arrange
        var player = new PlayerProgress(1L, "ana", 1, 0, 100, 0, 0, LocalDate.now());
        when(progress.findByUsername("ana")).thenReturn(Optional.of(player));

        // Act
        var spent = facade.spendCoins("ana", 150, "cpn-5", "5% off");

        // Assert
        assertFalse(spent);
        assertEquals(100, player.getSafeCoins());
        verify(progress, never()).save(any());
        verify(coinSpends, never()).save(any());
    }

    @Test
    @DisplayName("spendCoins should refuse non-positive amounts and unknown players (AAA)")
    void spendCoins_InvalidRequests_ReturnFalse() {
        // Arrange
        var player = new PlayerProgress(1L, "ana", 1, 0, 100, 0, 0, LocalDate.now());
        when(progress.findByUsername("ana")).thenReturn(Optional.of(player));
        when(progress.findByUsername("ghost")).thenReturn(Optional.empty());

        // Act + Assert
        assertFalse(facade.spendCoins("ana", 0, "cpn-5", "5% off"));
        assertFalse(facade.spendCoins("ghost", 10, "cpn-5", "5% off"));
        verify(coinSpends, never()).save(any());
    }
}
