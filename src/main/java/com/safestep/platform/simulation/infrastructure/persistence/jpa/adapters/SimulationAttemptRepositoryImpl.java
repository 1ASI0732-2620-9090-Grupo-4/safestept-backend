package com.safestep.platform.simulation.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.simulation.domain.model.aggregates.SimulationAttempt;
import com.safestep.platform.simulation.domain.repositories.SimulationAttemptRepository;
import com.safestep.platform.simulation.infrastructure.persistence.jpa.assemblers.SimulationPersistenceAssembler;
import com.safestep.platform.simulation.infrastructure.persistence.jpa.repositories.SimulationAttemptPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
public class SimulationAttemptRepositoryImpl implements SimulationAttemptRepository {
    private final SimulationAttemptPersistenceRepository repository;
    private final ApplicationEventPublisher publisher;

    public SimulationAttemptRepositoryImpl(SimulationAttemptPersistenceRepository repository,
            ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public SimulationAttempt save(SimulationAttempt value) {
        var saved = SimulationPersistenceAssembler
                .toDomain(repository.save(SimulationPersistenceAssembler.toPersistence(value)));
        value.domainEvents().forEach(publisher::publishEvent);
        value.clearDomainEvents();
        return saved;
    }

    @Transactional(readOnly = true)
    public List<SimulationAttempt> findByUsername(String username) {
        return repository.findByUsernameOrderByStartedAtDesc(username).stream()
                .map(SimulationPersistenceAssembler::toDomain).toList();
    }

    public boolean existsByExternalId(String id) {
        return repository.existsByExternalId(id);
    }
}
