package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.CartItemPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CartItemPersistenceRepository extends JpaRepository<CartItemPersistenceEntity, Long> {
    List<CartItemPersistenceEntity> findByUsername(String u);

    Optional<CartItemPersistenceEntity> findByExternalIdAndUsername(String id, String u);

    boolean existsByExternalId(String id);

    void deleteByUsername(String u);
}
