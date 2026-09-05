package com.safestep.platform.simulation.interfaces.acl;

import java.time.*;
import java.util.*;

public interface SimulationContextFacade {
    List<AttemptSnapshot> attemptsByUsername(String username);

    record AttemptSnapshot(String id, String simulationSlug, int score, int totalSteps, int correctSteps,
            long timeElapsed, Instant completedAt, List<String> errors) {
    }
}
