package com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.gamification.infrastructure.persistence.jpa.entities.CoinSpendPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CoinSpendPersistenceRepository extends JpaRepository<CoinSpendPersistenceEntity, Long> {
    boolean existsByExternalId(String id);

    List<CoinSpendPersistenceEntity> findByUsernameOrderBySpentAtDesc(String username);
}
