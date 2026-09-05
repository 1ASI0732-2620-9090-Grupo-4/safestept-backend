package com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.ShippingAddressPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ShippingAddressPersistenceRepository extends JpaRepository<ShippingAddressPersistenceEntity, Long> {
    boolean existsByExternalId(String id);

    List<ShippingAddressPersistenceEntity> findByUsername(String username);
}
