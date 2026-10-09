package com.safestep.platform.gamification.interfaces.rest.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.safestep.platform.gamification.domain.model.aggregates.Badge;
import com.safestep.platform.gamification.domain.model.aggregates.CoinTransaction;
import com.safestep.platform.gamification.domain.model.aggregates.Mission;
import com.safestep.platform.gamification.domain.model.aggregates.PlayerProgress;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.BadgeRarity;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.MissionCadence;
import com.safestep.platform.gamification.interfaces.rest.resources.BadgeResource;
import com.safestep.platform.gamification.interfaces.rest.resources.MissionResource;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GamificationResourceAssemblerTest {

    private final PlayerProgress progress = new PlayerProgress(1L, "ana", 3, 2400, 150, 4, 12, LocalDate.now());

    @Test
    @DisplayName("toResource(PlayerProgress) should map the summary fields (AAA)")
    void toResource_PlayerProgress_MapsSummary() {
        // Act
        var resource = GamificationResourceAssembler.toResource(progress);

        // Assert
        assertEquals("ana", resource.username());
        assertEquals(3, resource.level());
        assertEquals(2400, resource.xp());
        assertEquals(150, resource.safeCoins());
        assertEquals(4, resource.streak());
        assertEquals(12, resource.completedSimulations());
    }

    @Test
    @DisplayName("toResource(PlayerProgress, rank) should build a leaderboard entry (AAA)")
    void toResource_Leaderboard_MapsRank() {
        // Act
        var resource = GamificationResourceAssembler.toResource(progress, 2);

        // Assert
        assertEquals(2, resource.rank());
        assertEquals("ana", resource.name());
        assertEquals(2400, resource.xp());
        assertEquals(4, resource.streak());
    }

    @Test
    @DisplayName("mission mapping should round trip cadence, rewards and progress (AAA)")
    void mission_RoundTrip() {
        // Arrange
        var mission = new Mission(1L, "m-1", "Weekly CPR", MissionCadence.WEEKLY, 5, 200, 50, "ACTIVE",
                "Complete five CPR simulations", "Level 2");

        // Act
        var resource = GamificationResourceAssembler.toResource(mission, 3);
        var rebuilt = GamificationResourceAssembler.toMission(new MissionResource("m-2", "Monthly", "mensual", 0,
                10, 300, 80, "LOCKED", "Do ten", "Level 5"));

        // Assert
        assertEquals("WEEKLY", resource.cadence());
        assertEquals(3, resource.progress());
        assertEquals(5, resource.goal());
        assertEquals(50, resource.rewardCoins());
        assertEquals(MissionCadence.MONTHLY, rebuilt.getCadence());
        assertEquals("m-2", rebuilt.getExternalId());
        assertEquals("LOCKED", rebuilt.getStatus());
    }

    @Test
    @DisplayName("badge mapping should round trip rarity and unlocked flag (AAA)")
    void badge_RoundTrip() {
        // Arrange
        var badge = new Badge(1L, "b-1", "First Aid Hero", BadgeRarity.EPIC, "Finish ten simulations", "10 sims");

        // Act
        var resource = GamificationResourceAssembler.toResource(badge, true);
        var rebuilt = GamificationResourceAssembler
                .toBadge(new BadgeResource("b-2", "Starter", "legendary", false, "Welcome", "none"));

        // Assert
        assertEquals("EPIC", resource.rarity());
        assertTrue(resource.unlocked());
        assertEquals("b-1", resource.id());
        assertEquals(BadgeRarity.LEGENDARY, rebuilt.getRarity());
        assertEquals("b-2", rebuilt.getExternalId());
    }

    @Test
    @DisplayName("toResource(CoinTransaction) should always flag the transaction as successful (AAA)")
    void toResource_CoinTransaction_MapsReward() {
        // Arrange
        var at = Instant.parse("2026-09-14T15:00:00Z");
        var transaction = new CoinTransaction(1L, "tx-1", "ana", "cpr-adult", "Adult CPR", 30, 24, 80, 0.8, 0.8, at);

        // Act
        var resource = GamificationResourceAssembler.toResource(transaction);

        // Assert
        assertEquals("tx-1", resource.id());
        assertEquals("ana", resource.userId());
        assertEquals(30, resource.baseCoins());
        assertEquals(24, resource.earnedCoins());
        assertEquals(0.8, resource.multiplier());
        assertTrue(resource.successful());
        assertEquals(at, resource.createdAt());
    }
}
