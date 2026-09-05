package com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.gamification.infrastructure.persistence.jpa.entities.PlayerProgressPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PlayerProgressPersistenceRepository extends JpaRepository<PlayerProgressPersistenceEntity, Long> {
    Optional<PlayerProgressPersistenceEntity> findByUsername(String username);

    List<PlayerProgressPersistenceEntity> findAllByOrderByXpDesc();
}
