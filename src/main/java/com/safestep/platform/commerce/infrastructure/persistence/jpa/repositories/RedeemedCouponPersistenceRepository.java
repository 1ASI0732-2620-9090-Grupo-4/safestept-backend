package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.RedeemedCouponPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RedeemedCouponPersistenceRepository extends JpaRepository<RedeemedCouponPersistenceEntity, Long> {
    Optional<RedeemedCouponPersistenceEntity> findByExternalId(String id);

    List<RedeemedCouponPersistenceEntity> findByUsernameOrderByRedeemedAtDesc(String username);

    boolean existsByExternalId(String id);
}
