package com.safestep.platform.simulation.application.internal.outboundservices.acl;

import com.safestep.platform.simulation.domain.repositories.SimulationAttemptRepository;
import com.safestep.platform.simulation.interfaces.acl.SimulationContextFacade;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SimulationContextFacadeImpl implements SimulationContextFacade {
    private final SimulationAttemptRepository attempts;

    public SimulationContextFacadeImpl(SimulationAttemptRepository r) {
        attempts = r;
    }

    public List<AttemptSnapshot> attemptsByUsername(String u) {
        return attempts.findByUsername(u).stream()
                .map(a -> new AttemptSnapshot(a.getExternalId(), a.getSimulationSlug(), a.getScore(), a.getTotalSteps(),
                        a.getCorrectSteps(), a.getTimeElapsed(), a.getCompletedAt(),
                        a.getErrors().stream().map(e -> e.description()).toList()))
                .toList();
    }
}
