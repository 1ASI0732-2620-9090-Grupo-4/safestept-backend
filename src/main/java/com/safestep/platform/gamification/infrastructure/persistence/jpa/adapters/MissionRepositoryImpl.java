package com.safestep.platform.gamification.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.gamification.domain.model.aggregates.Mission;
import com.safestep.platform.gamification.domain.repositories.MissionRepository;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.assemblers.GamificationPersistenceAssembler;
import com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories.MissionPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class MissionRepositoryImpl implements MissionRepository {
    private final MissionPersistenceRepository repository;

    public MissionRepositoryImpl(MissionPersistenceRepository r) {
        repository = r;
    }

    public List<Mission> findAll() {
        return repository.findAll().stream().map(GamificationPersistenceAssembler::toDomain).toList();
    }

    public Optional<Mission> findByExternalId(String id) {
        return repository.findByExternalId(id).map(GamificationPersistenceAssembler::toDomain);
    }

    public Mission save(Mission d) {
        var s = repository.save(GamificationPersistenceAssembler.toEntity(d));
        d.setId(s.getId());
        return d;
    }

    public boolean existsByExternalId(String id) {
        return repository.existsByExternalId(id);
    }

    public void delete(Mission value) {
        repository.delete(GamificationPersistenceAssembler.toEntity(value));
    }
}
