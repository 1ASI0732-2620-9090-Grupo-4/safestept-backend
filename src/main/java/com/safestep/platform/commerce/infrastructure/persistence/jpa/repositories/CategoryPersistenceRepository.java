package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.CategoryPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryPersistenceRepository extends JpaRepository<CategoryPersistenceEntity, Long> {
    boolean existsByExternalId(String id);
}
