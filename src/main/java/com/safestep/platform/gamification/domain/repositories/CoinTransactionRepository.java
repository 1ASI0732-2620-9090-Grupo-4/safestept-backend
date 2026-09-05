package com.safestep.platform.gamification.domain.repositories;

import com.safestep.platform.gamification.domain.model.aggregates.CoinTransaction;
import java.util.*;

public interface CoinTransactionRepository {
    List<CoinTransaction> findByUsername(String username);

    CoinTransaction save(CoinTransaction value);

    boolean existsByExternalId(String id);
}
