package com.safestep.platform.gamification.domain.repositories;

import com.safestep.platform.gamification.domain.model.aggregates.CoinSpend;
import java.util.*;

public interface CoinSpendRepository {
    List<CoinSpend> findByUsername(String username);

    CoinSpend save(CoinSpend value);

    boolean existsByExternalId(String id);
}
