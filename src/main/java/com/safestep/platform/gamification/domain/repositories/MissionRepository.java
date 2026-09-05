package com.safestep.platform.gamification.domain.repositories;

import com.safestep.platform.gamification.domain.model.aggregates.Mission;
import java.util.*;

public interface MissionRepository {
    List<Mission> findAll();

    Optional<Mission> findByExternalId(String id);

    Mission save(Mission value);

    boolean existsByExternalId(String id);

    void delete(Mission value);
}
