package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.OrderPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface OrderPersistenceRepository extends JpaRepository<OrderPersistenceEntity, Long> {
    List<OrderPersistenceEntity> findByUsernameOrderByOrderedAtDesc(String u);

    Optional<OrderPersistenceEntity> findByExternalId(String id);

    Optional<OrderPersistenceEntity> findByStripeCheckoutSessionId(String sessionId);

    boolean existsByExternalId(String id);
}
