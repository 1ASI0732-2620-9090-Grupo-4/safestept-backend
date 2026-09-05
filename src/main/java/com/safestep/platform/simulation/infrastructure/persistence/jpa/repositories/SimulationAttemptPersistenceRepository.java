package com.safestep.platform.simulation.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.simulation.infrastructure.persistence.jpa.entities.SimulationAttemptPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SimulationAttemptPersistenceRepository
        extends JpaRepository<SimulationAttemptPersistenceEntity, Long> {
    List<SimulationAttemptPersistenceEntity> findByUsernameOrderByStartedAtDesc(String username);

    boolean existsByExternalId(String externalId);
}
