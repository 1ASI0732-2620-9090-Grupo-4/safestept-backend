package com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.gamification.infrastructure.persistence.jpa.entities.MissionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MissionPersistenceRepository extends JpaRepository<MissionPersistenceEntity, Long> {
    Optional<MissionPersistenceEntity> findByExternalId(String id);

    boolean existsByExternalId(String id);
}
