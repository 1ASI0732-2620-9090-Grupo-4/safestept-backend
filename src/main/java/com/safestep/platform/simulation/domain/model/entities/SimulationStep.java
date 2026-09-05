package com.safestep.platform.simulation.domain.model.entities;

import java.util.List;

public record SimulationStep(String externalId, String prompt, String correctOptionId, List<SimulationOption> options) {

    public SimulationStep {
        options = options == null ? List.of() : List.copyOf(options);
    }
    public record SimulationOption(String externalId, String label, String feedback) {
    }
}
