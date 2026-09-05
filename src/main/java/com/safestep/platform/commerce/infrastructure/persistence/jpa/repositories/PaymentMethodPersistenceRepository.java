package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.PaymentMethodPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodPersistenceRepository extends JpaRepository<PaymentMethodPersistenceEntity, Long> {
    boolean existsByExternalId(String id);
}
