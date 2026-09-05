package com.safestep.platform.simulation.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.simulation.infrastructure.persistence.jpa.entities.MedicalSimulationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MedicalSimulationPersistenceRepository
        extends JpaRepository<MedicalSimulationPersistenceEntity, Long> {
    Optional<MedicalSimulationPersistenceEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
