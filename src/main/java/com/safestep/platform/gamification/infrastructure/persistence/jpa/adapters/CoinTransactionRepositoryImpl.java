package com.safestep.platform.gamification.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.gamification.domain.model.aggregates.CoinTransaction;
import com.safestep.platform.gamification.domain.repositories.CoinTransactionRepository;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.assemblers.GamificationPersistenceAssembler;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories.CoinTransactionPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class CoinTransactionRepositoryImpl implements CoinTransactionRepository {
    private final CoinTransactionPersistenceRepository repository;

    public CoinTransactionRepositoryImpl(CoinTransactionPersistenceRepository r) {
        repository = r;
    }

    public List<CoinTransaction> findByUsername(String u) {
        return repository.findByUsernameOrderByOccurredAtDesc(u).stream()
                .map(GamificationPersistenceAssembler::toDomain).toList();
    }

    public CoinTransaction save(CoinTransaction d) {
        var s = repository.save(GamificationPersistenceAssembler.toEntity(d));
        d.setId(s.getId());
        return d;
    }

    public boolean existsByExternalId(String id) {
        return repository.existsByExternalId(id);
    }
}
