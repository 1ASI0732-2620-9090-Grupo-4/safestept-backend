package com.safestep.platform.simulation.application.commandservices;

import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import com.safestep.platform.simulation.domain.model.aggregates.MedicalSimulation;
import com.safestep.platform.simulation.domain.model.commands.CreateSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.DeleteSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.UpdateSimulationCommand;

public interface SimulationCommandService {
    Result<MedicalSimulation, ApplicationError> handle(CreateSimulationCommand command);

    Result<MedicalSimulation, ApplicationError> handle(UpdateSimulationCommand command);

    Result<String, ApplicationError> handle(DeleteSimulationCommand command);
}
