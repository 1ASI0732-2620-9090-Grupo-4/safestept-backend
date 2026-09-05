package com.safestep.platform.simulation.application.internal.eventhandlers;

import com.safestep.platform.simulation.domain.model.aggregates.*;
import com.safestep.platform.simulation.domain.model.entities.*;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.*;
import com.safestep.platform.simulation.domain.repositories.*;
import com.safestep.platform.simulation.interfaces.rest.resources.AttemptErrorResource;
import com.safestep.platform.simulation.interfaces.rest.resources.SimulationResource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;

@Service
public class SimulationSeedEventHandler {
    private final MedicalSimulationRepository simulations;
    private final SimulationAttemptRepository attempts;
    private final ObjectMapper mapper;

    public SimulationSeedEventHandler(MedicalSimulationRepository s, SimulationAttemptRepository a, ObjectMapper m) {
        simulations = s;
        attempts = a;
        mapper = m;
    }

    @EventListener
    public void on(ApplicationReadyEvent ignored) {
        try (var in = new ClassPathResource("safestep-seed.json").getInputStream()) {
            var seed = mapper.readValue(in, Seed.class);
            for (var r : seed.simulations())
                if (!simulations.existsBySlug(r.id()))
                    simulations.save(toDomain(r));
            for (var r : seed.attempts()) {
                var external = "seed-attempt-" + r.id();
                if (!attempts.existsByExternalId(external))
                    attempts.save(new SimulationAttempt(null, external, "backend.check", r.scenarioSlug(),
                            AttemptMode.from(r.mode()), r.startedAt(), r.completedAt(), r.score(), r.totalSteps(),
                            r.correctSteps(), r.timeElapsed(), AttemptStatus.COMPLETED,
                            r.errors() == null ? List.of()
                                    : r.errors().stream()
                                            .map(x -> new AttemptError(x.stepNumber(), x.error(), x.severity()))
                                            .toList()));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Simulation seed could not be loaded", e);
        }
    }

    private MedicalSimulation toDomain(SimulationResource r) {
        return new MedicalSimulation(null, r.id(), r.title(), r.emergencyType(), Difficulty.from(r.difficulty()),
                r.durationMinutes(), r.description(), r.imageUrl(), r.status(), r.completion(),
                new SimulationReward(r.xpReward(), r.coinReward()), r.learningGoals(),
                r.steps().stream()
                        .map(s -> new SimulationStep(s.id(), s.prompt(), s.correctOptionId(),
                                s.options().stream()
                                        .map(o -> new SimulationStep.SimulationOption(o.id(), o.label(), o.feedback()))
                                        .toList()))
                        .toList(),
                r.productSuggestions().stream()
                        .map(x -> new MedicalSimulation.ProductSuggestion(x.productId(), x.reason())).toList());
    }

    private record Seed(List<SimulationResource> simulations, List<AttemptSeed> attempts) {
    }

    private record AttemptSeed(Object id, String mode, Instant startedAt, Instant completedAt, int score,
            int totalSteps, int correctSteps, long timeElapsed, List<AttemptErrorResource> errors,
            String scenarioSlug) {
    }
}
