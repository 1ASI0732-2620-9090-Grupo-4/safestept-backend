package com.safestep.platform.simulation.domain.model.commands;

import com.safestep.platform.simulation.domain.model.aggregates.MedicalSimulation;

public record CreateSimulationCommand(MedicalSimulation simulation) {
}
