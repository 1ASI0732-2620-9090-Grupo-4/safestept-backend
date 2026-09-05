package com.safestep.platform.simulation.application.internal.commandservices;

import com.safestep.platform.shared.application.result.*;
import com.safestep.platform.simulation.domain.model.aggregates.*;
import com.safestep.platform.simulation.domain.model.commands.CreateSimulationAttemptCommand;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.*;
import com.safestep.platform.simulation.domain.repositories.*;
import com.safestep.platform.simulation.application.commandservices.SimulationAttemptCommandService;
import org.springframework.stereotype.Service;

@Service
public class SimulationAttemptCommandServiceImpl implements SimulationAttemptCommandService {

    private final MedicalSimulationRepository simulationRepository;
    private final SimulationAttemptRepository attemptRepository;

    public SimulationAttemptCommandServiceImpl(MedicalSimulationRepository simulationRepository,
            SimulationAttemptRepository attemptRepository) {
        this.simulationRepository = simulationRepository;
        this.attemptRepository = attemptRepository;
    }

    @Override
    public Result<SimulationAttempt, ApplicationError> handle(CreateSimulationAttemptCommand c) {
        var simulation = simulationRepository.findBySlug(c.simulationSlug());
        if (simulation.isEmpty())
            return Result.failure(ApplicationError.notFound("Simulation", c.simulationSlug()));
        var id = "attempt-" + java.util.UUID.randomUUID();
        var attempt = new SimulationAttempt(null, id, c.username(), c.simulationSlug(), AttemptMode.from(c.mode()),
                c.startedAt() == null ? java.time.Instant.now() : c.startedAt(), c.completedAt(), c.score(),
                c.totalSteps(), c.correctSteps(), c.timeElapsed(), AttemptStatus.COMPLETED, c.errors());
        attempt.markCompleted(simulation.get());
        return Result.success(attemptRepository.save(attempt));
    }
}
