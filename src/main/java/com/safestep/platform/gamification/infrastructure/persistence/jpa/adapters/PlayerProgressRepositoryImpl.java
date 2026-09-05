package com.safestep.platform.gamification.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.gamification.domain.model.aggregates.PlayerProgress;
import com.safestep.platform.gamification.domain.repositories.PlayerProgressRepository;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.assemblers.GamificationPersistenceAssembler;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories.PlayerProgressPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class PlayerProgressRepositoryImpl implements PlayerProgressRepository {
    private final PlayerProgressPersistenceRepository repository;

    public PlayerProgressRepositoryImpl(PlayerProgressPersistenceRepository r) {
        repository = r;
    }

    public Optional<PlayerProgress> findByUsername(String u) {
        return repository.findByUsername(u).map(GamificationPersistenceAssembler::toDomain);
    }

    public List<PlayerProgress> findLeaderboard() {
        return repository.findAllByOrderByXpDesc().stream().map(GamificationPersistenceAssembler::toDomain).toList();
    }

    public PlayerProgress save(PlayerProgress d) {
        var saved = repository.save(GamificationPersistenceAssembler.toEntity(d));
        d.setId(saved.getId());
        return d;
    }
}
