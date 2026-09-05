package com.safestep.platform.simulation.application.internal.commandservices;

import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import com.safestep.platform.simulation.application.commandservices.SimulationCommandService;
import com.safestep.platform.simulation.domain.model.aggregates.MedicalSimulation;
import com.safestep.platform.simulation.domain.model.commands.CreateSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.DeleteSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.UpdateSimulationCommand;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.SimulationReward;
import com.safestep.platform.simulation.domain.repositories.MedicalSimulationRepository;
import org.springframework.stereotype.Service;

@Service
public class SimulationCommandServiceImpl implements SimulationCommandService {
    private final MedicalSimulationRepository simulations;

    public SimulationCommandServiceImpl(MedicalSimulationRepository simulations) {
        this.simulations = simulations;
    }

    @Override
    public Result<MedicalSimulation, ApplicationError> handle(CreateSimulationCommand command) {
        try {
            var simulation = command.simulation();
            if (simulations.existsBySlug(simulation.getSlug()))
                return Result.failure(ApplicationError.conflict("simulation", "Simulation id already exists"));
            return Result.success(simulations.save(simulation));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("simulation", e.getMessage()));
        }
    }

    @Override
    public Result<MedicalSimulation, ApplicationError> handle(UpdateSimulationCommand command) {
        try {
            if (command.simulationId() == null || command.simulationId().isBlank())
                return Result.failure(ApplicationError.validationError("simulation", "Simulation id is required"));
            var found = simulations.findBySlug(command.simulationId());
            if (found.isEmpty())
                return Result.failure(ApplicationError.notFound("simulation", command.simulationId()));
            var source = command.simulation();
            var simulation = new MedicalSimulation(found.get().getId(), command.simulationId(), source.getTitle(),
                    source.getEmergencyType(), source.getDifficulty(), source.getDurationMinutes(),
                    source.getDescription(), source.getImageUrl(), source.getStatus(), source.getCompletion(),
                    new SimulationReward(source.getReward().xp(), source.getReward().coins()),
                    source.getLearningGoals(), source.getSteps(), source.getProductSuggestions());
            return Result.success(simulations.save(simulation));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("simulation", e.getMessage()));
        }
    }

    @Override
    public Result<String, ApplicationError> handle(DeleteSimulationCommand command) {
        var found = simulations.findBySlug(command.simulationId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("simulation", command.simulationId()));
        simulations.delete(found.get());
        return Result.success("Simulation deleted");
    }
}
