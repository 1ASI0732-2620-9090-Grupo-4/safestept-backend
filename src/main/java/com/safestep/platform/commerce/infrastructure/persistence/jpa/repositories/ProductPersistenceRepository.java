package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.ProductPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProductPersistenceRepository extends JpaRepository<ProductPersistenceEntity, Long> {
    Optional<ProductPersistenceEntity> findByExternalId(String id);

    boolean existsByExternalId(String id);
}
