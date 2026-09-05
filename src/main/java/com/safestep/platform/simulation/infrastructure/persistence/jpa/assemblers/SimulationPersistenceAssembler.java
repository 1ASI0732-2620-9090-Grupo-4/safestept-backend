package com.safestep.platform.simulation.infrastructure.persistence.jpa.assemblers;

import com.safestep.platform.simulation.domain.model.aggregates.*;
import com.safestep.platform.simulation.domain.model.entities.*;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.*;
import com.safestep.platform.simulation.infrastructure.persistence.jpa.entities.*;

public final class SimulationPersistenceAssembler {
    private SimulationPersistenceAssembler() {
    }

    public static MedicalSimulation toDomain(MedicalSimulationPersistenceEntity e) {
        return new MedicalSimulation(e.getId(), e.getSlug(), e.getTitle(), e.getEmergencyType(),
                Difficulty.from(e.getDifficulty()), e.getDurationMinutes(), e.getDescription(), e.getImageUrl(),
                e.getStatus(), e.getCompletion(), new SimulationReward(e.getXpReward(), e.getCoinReward()),
                e.getLearningGoals(),
                e.getSteps().stream()
                        .map(s -> new SimulationStep(s.getExternalId(), s.getPrompt(), s.getCorrectOptionId(),
                                e.getOptions().stream().filter(o -> s.getExternalId().equals(o.getStepExternalId()))
                                        .map(o -> new SimulationStep.SimulationOption(o.getExternalId(), o.getLabel(),
                                                o.getFeedback()))
                                        .toList()))
                        .toList(),
                e.getProductSuggestions().stream()
                        .map(p -> new MedicalSimulation.ProductSuggestion(p.getProductId(), p.getReason())).toList());
    }

    public static MedicalSimulationPersistenceEntity toPersistence(MedicalSimulation d) {
        var e = new MedicalSimulationPersistenceEntity();
        e.setId(d.getId());
        e.setSlug(d.getSlug());
        e.setTitle(d.getTitle());
        e.setEmergencyType(d.getEmergencyType());
        e.setDifficulty(d.getDifficulty().name());
        e.setDurationMinutes(d.getDurationMinutes());
        e.setDescription(d.getDescription());
        e.setImageUrl(d.getImageUrl());
        e.setStatus(d.getStatus());
        e.setCompletion(d.getCompletion());
        e.setXpReward(d.getReward().xp());
        e.setCoinReward(d.getReward().coins());
        e.setLearningGoals(d.getLearningGoals());
        e.setSteps(d.getSteps().stream().map(s -> {
            var x = new MedicalSimulationPersistenceEntity.StepEmbeddable();
            x.setExternalId(s.externalId());
            x.setPrompt(s.prompt());
            x.setCorrectOptionId(s.correctOptionId());
            return x;
        }).toList());
        e.setOptions(d.getSteps().stream().flatMap(s -> s.options().stream().map(o -> {
            var y = new MedicalSimulationPersistenceEntity.OptionEmbeddable();
            y.setStepExternalId(s.externalId());
            y.setExternalId(o.externalId());
            y.setLabel(o.label());
            y.setFeedback(o.feedback());
            return y;
        })).toList());
        e.setProductSuggestions(d.getProductSuggestions().stream().map(p -> {
            var x = new MedicalSimulationPersistenceEntity.ProductSuggestionEmbeddable();
            x.setProductId(p.productId());
            x.setReason(p.reason());
            return x;
        }).toList());
        return e;
    }

    public static SimulationAttempt toDomain(SimulationAttemptPersistenceEntity e) {
        return new SimulationAttempt(e.getId(), e.getExternalId(), e.getUsername(), e.getSimulationSlug(),
                AttemptMode.from(e.getMode()), e.getStartedAt(), e.getCompletedAt(), e.getScore(), e.getTotalSteps(),
                e.getCorrectSteps(), e.getTimeElapsed(), AttemptStatus.valueOf(e.getStatus()), e.getErrors().stream()
                        .map(x -> new AttemptError(x.getStepNumber(), x.getDescription(), x.getSeverity())).toList());
    }

    public static SimulationAttemptPersistenceEntity toPersistence(SimulationAttempt d) {
        var e = new SimulationAttemptPersistenceEntity();
        e.setId(d.getId());
        e.setExternalId(d.getExternalId());
        e.setUsername(d.getUsername());
        e.setSimulationSlug(d.getSimulationSlug());
        e.setMode(d.getMode().name());
        e.setStartedAt(d.getStartedAt());
        e.setCompletedAt(d.getCompletedAt());
        e.setScore(d.getScore());
        e.setTotalSteps(d.getTotalSteps());
        e.setCorrectSteps(d.getCorrectSteps());
        e.setTimeElapsed(d.getTimeElapsed());
        e.setStatus(d.getStatus().name());
        e.setErrors(d.getErrors().stream().map(x -> {
            var y = new SimulationAttemptPersistenceEntity.AttemptErrorEmbeddable();
            y.setStepNumber(x.stepNumber());
            y.setDescription(x.description());
            y.setSeverity(x.severity());
            return y;
        }).toList());
        return e;
    }
}
