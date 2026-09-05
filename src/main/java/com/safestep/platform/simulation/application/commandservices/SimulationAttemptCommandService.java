package com.safestep.platform.simulation.application.commandservices;

import com.safestep.platform.shared.application.result.*;
import com.safestep.platform.simulation.domain.model.aggregates.SimulationAttempt;
import com.safestep.platform.simulation.domain.model.commands.CreateSimulationAttemptCommand;

public interface SimulationAttemptCommandService {
    Result<SimulationAttempt, ApplicationError> handle(CreateSimulationAttemptCommand command);
}
