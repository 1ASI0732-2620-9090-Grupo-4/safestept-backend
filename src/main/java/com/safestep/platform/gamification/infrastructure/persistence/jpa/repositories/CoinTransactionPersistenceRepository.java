package com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.gamification.infrastructure.persistence.jpa.entities.CoinTransactionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CoinTransactionPersistenceRepository extends JpaRepository<CoinTransactionPersistenceEntity, Long> {
    boolean existsByExternalId(String id);

    List<CoinTransactionPersistenceEntity> findByUsernameOrderByOccurredAtDesc(String username);
}
