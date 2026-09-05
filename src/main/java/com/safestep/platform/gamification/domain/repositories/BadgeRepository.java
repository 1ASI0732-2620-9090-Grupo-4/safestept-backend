package com.safestep.platform.gamification.domain.repositories;

import com.safestep.platform.gamification.domain.model.aggregates.Badge;
import java.util.*;

public interface BadgeRepository {
    List<Badge> findAll();

    Optional<Badge> findByExternalId(String id);

    Badge save(Badge value);

    boolean existsByExternalId(String id);

    void delete(Badge value);
}
