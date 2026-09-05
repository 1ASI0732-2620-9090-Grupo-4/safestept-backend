package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.ProductRecommendationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProductRecommendationPersistenceRepository
        extends JpaRepository<ProductRecommendationPersistenceEntity, Long> {
    boolean existsByExternalId(String id);

    List<ProductRecommendationPersistenceEntity> findByUsername(String username);
}
