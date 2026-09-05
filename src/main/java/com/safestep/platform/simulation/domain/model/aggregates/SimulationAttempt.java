package com.safestep.platform.simulation.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.safestep.platform.simulation.domain.model.entities.AttemptError;
import com.safestep.platform.simulation.domain.model.events.SimulationAttemptCompletedEvent;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.AttemptMode;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.AttemptStatus;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.Score;

import java.time.Instant;
import java.util.List;

public class SimulationAttempt extends AbstractDomainAggregateRoot<SimulationAttempt> {
    private Long id;
    private final String externalId;
    private final String username;
    private final String simulationSlug;
    private final AttemptMode mode;
    private final Instant startedAt;
    private Instant completedAt;
    private Score score;
    private int totalSteps;
    private int correctSteps;
    private long timeElapsed;
    private AttemptStatus status;
    private List<AttemptError> errors;

    public SimulationAttempt(Long id, String externalId, String username, String simulationSlug, AttemptMode mode,
            Instant startedAt, Instant completedAt, int score, int totalSteps, int correctSteps, long timeElapsed,
            AttemptStatus status, List<AttemptError> errors) {
        this.id = id;
        this.externalId = externalId;
        this.username = username;
        this.simulationSlug = simulationSlug;
        this.mode = mode;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.score = new Score(score);
        this.totalSteps = totalSteps;
        this.correctSteps = correctSteps;
        this.timeElapsed = timeElapsed;
        this.status = status;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public void markCompleted(MedicalSimulation simulation) {
        status = AttemptStatus.COMPLETED;
        if (completedAt == null)
            completedAt = Instant.now();
        double accuracy = totalSteps == 0 ? score.value() / 100.0 : (double) correctSteps / totalSteps;
        registerDomainEvent(new SimulationAttemptCompletedEvent(externalId, username, simulationSlug,
                simulation.getTitle(), score.value(), accuracy, simulation.getReward().xp(),
                simulation.getReward().coins(), Instant.now()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getUsername() {
        return username;
    }

    public String getSimulationSlug() {
        return simulationSlug;
    }

    public AttemptMode getMode() {
        return mode;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public int getScore() {
        return score.value();
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getCorrectSteps() {
        return correctSteps;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public List<AttemptError> getErrors() {
        return errors;
    }
}
