package com.safestep.platform.gamification.domain.repositories;

import com.safestep.platform.gamification.domain.model.aggregates.PlayerProgress;
import java.util.*;

public interface PlayerProgressRepository {
    Optional<PlayerProgress> findByUsername(String username);

    List<PlayerProgress> findLeaderboard();

    PlayerProgress save(PlayerProgress value);
}
