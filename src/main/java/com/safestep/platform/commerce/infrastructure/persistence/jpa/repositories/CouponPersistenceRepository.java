package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.CouponPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CouponPersistenceRepository extends JpaRepository<CouponPersistenceEntity, Long> {
    Optional<CouponPersistenceEntity> findByExternalId(String id);

    boolean existsByExternalId(String id);
}
