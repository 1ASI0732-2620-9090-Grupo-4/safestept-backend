package com.safestep.platform.simulation.application.internal.queryservices;

import com.safestep.platform.simulation.domain.model.aggregates.*;
import com.safestep.platform.simulation.domain.model.queries.GetAllSimulationsQuery;
import com.safestep.platform.simulation.domain.model.queries.GetAttemptsByUsernameQuery;
import com.safestep.platform.simulation.domain.model.queries.GetSimulationBySlugQuery;
import com.safestep.platform.simulation.domain.repositories.*;
import com.safestep.platform.simulation.application.queryservices.SimulationQueryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SimulationQueryServiceImpl implements SimulationQueryService {

    private final MedicalSimulationRepository simulationRepository;
    private final SimulationAttemptRepository attemptRepository;

    public SimulationQueryServiceImpl(MedicalSimulationRepository simulationRepository,
            SimulationAttemptRepository attemptRepository) {
        this.simulationRepository = simulationRepository;
        this.attemptRepository = attemptRepository;
    }

    @Override
    public List<MedicalSimulation> handle(GetAllSimulationsQuery query) {
        return simulationRepository.findAll();
    }

    @Override
    public Optional<MedicalSimulation> handle(GetSimulationBySlugQuery query) {
        return simulationRepository.findBySlug(query.slug());
    }

    @Override
    public List<SimulationAttempt> handle(GetAttemptsByUsernameQuery query) {
        return attemptRepository.findByUsername(query.username());
    }
}
