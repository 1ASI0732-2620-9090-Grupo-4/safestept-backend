package com.safestep.platform.gamification.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.BadgeRarity;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.ExperiencePoints;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.MissionCadence;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.PlayerLevel;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.SafeCoins;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoinSpendAndValueObjectsTest {

    @Test
    @DisplayName("CoinSpend should keep the coupon and amount it was created with (AAA)")
    void coinSpend_KeepsItsData() {
        // Arrange
        var at = Instant.parse("2026-09-14T15:00:00Z");

        // Act
        var spend = new CoinSpend(1L, "spend-1", "ana", "cpn-5", "5% off", 150, at);
        spend.setId(9L);

        // Assert
        assertEquals(9L, spend.getId());
        assertEquals("spend-1", spend.getExternalId());
        assertEquals("ana", spend.getUsername());
        assertEquals("cpn-5", spend.getCouponId());
        assertEquals("5% off", spend.getCouponTitle());
        assertEquals(150, spend.getAmount());
        assertEquals(at, spend.getSpentAt());
    }

    @Test
    @DisplayName("PlayerProgress should reject non-positive and excessive coin spends (AAA)")
    void playerProgress_RejectsInvalidSpends() {
        // Arrange
        var player = new PlayerProgress(1L, "ana", 1, 0, 100, 0, 0, LocalDate.now());

        // Assert
        assertThrows(IllegalArgumentException.class, () -> player.spendCoins(0));
        assertThrows(IllegalArgumentException.class, () -> player.spendCoins(-5));
        assertThrows(IllegalStateException.class, () -> player.spendCoins(101));
        assertEquals(100, player.getSafeCoins());
    }

    @Test
    @DisplayName("PlayerProgress should normalise negative inputs and reset the streak after a gap (AAA)")
    void playerProgress_NormalisesInputsAndStreak() {
        // Arrange
        var player = new PlayerProgress(1L, "ana", 0, -10, -5, -1, -2, LocalDate.now().minusDays(5));

        // Act
        player.reward(1000, 10);

        // Assert
        assertEquals(1, player.getStreakDays(), "A gap of several days restarts the streak at one");
        assertEquals(2, player.getLevel());
        assertEquals(10, player.getSafeCoins());
        assertEquals(1, player.getCompletedSimulations());
    }

    @Test
    @DisplayName("gamification value objects should validate their ranges (AAA)")
    void valueObjects_ValidateRanges() {
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new ExperiencePoints(-1));
        assertThrows(IllegalArgumentException.class, () -> new SafeCoins(-1));
        assertThrows(IllegalArgumentException.class, () -> new PlayerLevel(0));
        assertEquals(MissionCadence.DAILY, MissionCadence.from(null));
        assertEquals(MissionCadence.WEEKLY, MissionCadence.from("semanal"));
        assertEquals(MissionCadence.MONTHLY, MissionCadence.from("monthly"));
        assertEquals(MissionCadence.DAILY, MissionCadence.from("other"));
        assertEquals(BadgeRarity.COMMON, BadgeRarity.from(null));
        assertEquals(BadgeRarity.LEGENDARY, BadgeRarity.from("legendary"));
        assertEquals(BadgeRarity.EPIC, BadgeRarity.from("epic"));
        assertEquals(BadgeRarity.RARE, BadgeRarity.from("rare"));
    }
}
