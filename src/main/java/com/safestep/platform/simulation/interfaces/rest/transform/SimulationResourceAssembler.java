package com.safestep.platform.simulation.interfaces.rest.transform;

import com.safestep.platform.simulation.domain.model.aggregates.*;
import com.safestep.platform.simulation.domain.model.commands.CreateSimulationAttemptCommand;
import com.safestep.platform.simulation.domain.model.entities.AttemptError;
import com.safestep.platform.simulation.domain.model.entities.SimulationStep;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.Difficulty;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.SimulationReward;
import com.safestep.platform.simulation.interfaces.rest.resources.*;

public final class SimulationResourceAssembler {
    private SimulationResourceAssembler() {
    }

    public static SimulationResource toResource(MedicalSimulation s) {
        return new SimulationResource(s.getSlug(), s.getTitle(), s.getEmergencyType(), s.getDifficulty().name(),
                s.getDurationMinutes(), s.getReward().xp(), s.getImageUrl(), s.getStatus(), s.getCompletion(),
                s.getDescription(), s.getLearningGoals(),
                s.getSteps().stream()
                        .map(x -> new StepResource(x.externalId(), x.prompt(), x.correctOptionId(), x.options().stream()
                                .map(o -> new OptionResource(o.externalId(), o.label(), o.feedback())).toList()))
                        .toList(),
                s.getProductSuggestions().stream().map(x -> new ProductSuggestionResource(x.productId(), x.reason()))
                        .toList(),
                s.getReward().coins());
    }

    public static CreateSimulationAttemptCommand toCommand(String slug, String username, CreateAttemptResource r) {
        return new CreateSimulationAttemptCommand(slug, username, r.mode(), r.startedAt(), r.completedAt(), r.score(),
                r.totalSteps(), r.correctSteps(), r.timeElapsed(),
                r.errors() == null ? java.util.List.of()
                        : r.errors().stream().map(x -> new AttemptError(x.stepNumber(), x.error(), x.severity()))
                                .toList());
    }

    public static MedicalSimulation toSimulation(SimulationResource r) {
        return new MedicalSimulation(null, r.id(), r.title(), r.emergencyType(), Difficulty.from(r.difficulty()),
                r.durationMinutes(), r.description(), r.imageUrl(), r.status(), r.completion(),
                new SimulationReward(r.xpReward(), r.coinReward()), r.learningGoals(),
                r.steps() == null ? java.util.List.of()
                        : r.steps().stream()
                                .map(step -> new SimulationStep(step.id(), step.prompt(), step.correctOptionId(),
                                        step.options() == null ? java.util.List.of()
                                                : step.options().stream()
                                                        .map(option -> new SimulationStep.SimulationOption(
                                                                option.id(), option.label(), option.feedback()))
                                                        .toList()))
                                .toList(),
                r.productSuggestions() == null ? java.util.List.of()
                        : r.productSuggestions().stream()
                                .map(product -> new MedicalSimulation.ProductSuggestion(product.productId(),
                                        product.reason()))
                                .toList());
    }

    public static SimulationAttemptResource toResource(SimulationAttempt a) {
        return new SimulationAttemptResource(a.getExternalId(), a.getUsername(), a.getSimulationSlug(),
                a.getMode().name().toLowerCase(), a.getStartedAt(), a.getCompletedAt(), a.getScore(), a.getTotalSteps(),
                a.getCorrectSteps(), a.getTimeElapsed(), a.getErrors().stream()
                        .map(x -> new AttemptErrorResource(x.stepNumber(), x.description(), x.severity())).toList(),
                a.getSimulationSlug());
    }
}
