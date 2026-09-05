package com.safestep.platform.gamification.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class PlayerProgressTest {
    @Test
    void rewardsExperienceCoinsAndActivity() {
        var player = new PlayerProgress(null, "ana", 1, 950, 20, 2, 4, LocalDate.now().minusDays(1));
        player.reward(100, 30);
        assertEquals(2, player.getLevel());
        assertEquals(1050, player.getXp());
        assertEquals(50, player.getSafeCoins());
        assertEquals(3, player.getStreakDays());
        assertEquals(5, player.getCompletedSimulations());
    }
}
