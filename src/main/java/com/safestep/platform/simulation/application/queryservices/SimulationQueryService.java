package com.safestep.platform.simulation.application.queryservices;

import com.safestep.platform.simulation.domain.model.aggregates.*;
import com.safestep.platform.simulation.domain.model.queries.GetAllSimulationsQuery;
import com.safestep.platform.simulation.domain.model.queries.GetAttemptsByUsernameQuery;
import com.safestep.platform.simulation.domain.model.queries.GetSimulationBySlugQuery;
import java.util.List;
import java.util.Optional;

public interface SimulationQueryService {
    List<MedicalSimulation> handle(GetAllSimulationsQuery query);

    Optional<MedicalSimulation> handle(GetSimulationBySlugQuery query);

    List<SimulationAttempt> handle(GetAttemptsByUsernameQuery query);
}
