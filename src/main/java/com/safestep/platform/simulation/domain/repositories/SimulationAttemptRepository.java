package com.safestep.platform.simulation.domain.repositories;

import com.safestep.platform.simulation.domain.model.aggregates.SimulationAttempt;
import java.util.*;

public interface SimulationAttemptRepository {
    SimulationAttempt save(SimulationAttempt attempt);

    List<SimulationAttempt> findByUsername(String username);

    boolean existsByExternalId(String externalId);
}
