package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.EmergencyKitPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyKitPersistenceRepository extends JpaRepository<EmergencyKitPersistenceEntity, Long> {
    boolean existsByExternalId(String id);
}
