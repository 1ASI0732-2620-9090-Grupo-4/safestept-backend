package com.safestep.platform.gamification.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.gamification.domain.model.aggregates.CoinSpend;
import com.safestep.platform.gamification.domain.repositories.CoinSpendRepository;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.assemblers.GamificationPersistenceAssembler;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories.CoinSpendPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class CoinSpendRepositoryImpl implements CoinSpendRepository {
    private final CoinSpendPersistenceRepository repository;

    public CoinSpendRepositoryImpl(CoinSpendPersistenceRepository r) {
        repository = r;
    }

    public List<CoinSpend> findByUsername(String u) {
        return repository.findByUsernameOrderBySpentAtDesc(u).stream().map(GamificationPersistenceAssembler::toDomain)
                .toList();
    }

    public CoinSpend save(CoinSpend d) {
        var s = repository.save(GamificationPersistenceAssembler.toEntity(d));
        d.setId(s.getId());
        return d;
    }

    public boolean existsByExternalId(String id) {
        return repository.existsByExternalId(id);
    }
}
