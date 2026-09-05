package com.safestep.platform.gamification.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.gamification.domain.model.aggregates.Badge;
import com.safestep.platform.gamification.domain.repositories.BadgeRepository;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.assemblers.GamificationPersistenceAssembler;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories.BadgePersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class BadgeRepositoryImpl implements BadgeRepository {
    private final BadgePersistenceRepository repository;

    public BadgeRepositoryImpl(BadgePersistenceRepository r) {
        repository = r;
    }

    public List<Badge> findAll() {
        return repository.findAll().stream().map(GamificationPersistenceAssembler::toDomain).toList();
    }

    public Optional<Badge> findByExternalId(String id) {
        return repository.findByExternalId(id).map(GamificationPersistenceAssembler::toDomain);
    }

    public Badge save(Badge d) {
        var s = repository.save(GamificationPersistenceAssembler.toEntity(d));
        d.setId(s.getId());
        return d;
    }

    public boolean existsByExternalId(String id) {
        return repository.existsByExternalId(id);
    }

    public void delete(Badge value) {
        repository.delete(GamificationPersistenceAssembler.toEntity(value));
    }
}
