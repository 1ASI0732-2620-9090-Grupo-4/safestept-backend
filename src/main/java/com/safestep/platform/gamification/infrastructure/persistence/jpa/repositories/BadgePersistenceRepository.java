package com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.gamification.infrastructure.persistence.jpa.entities.BadgePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BadgePersistenceRepository extends JpaRepository<BadgePersistenceEntity, Long> {
    Optional<BadgePersistenceEntity> findByExternalId(String id);

    boolean existsByExternalId(String id);
}
