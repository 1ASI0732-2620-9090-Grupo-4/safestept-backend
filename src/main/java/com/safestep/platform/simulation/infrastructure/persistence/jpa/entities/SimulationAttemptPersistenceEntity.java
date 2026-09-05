package com.safestep.platform.simulation.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "simulation_attempts")
public class SimulationAttemptPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true)
    private String externalId;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String simulationSlug;
    private String mode;
    private Instant startedAt;
    private Instant completedAt;
    private int score;
    private int totalSteps;
    private int correctSteps;
    private long timeElapsed;
    private String status;
    @ElementCollection
    @CollectionTable(name = "attempt_errors", joinColumns = @JoinColumn(name = "attempt_id"))
    private List<AttemptErrorEmbeddable> errors = new ArrayList<>();

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String v) {
        externalId = v;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        username = v;
    }

    public String getSimulationSlug() {
        return simulationSlug;
    }

    public void setSimulationSlug(String v) {
        simulationSlug = v;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String v) {
        mode = v;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant v) {
        startedAt = v;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant v) {
        completedAt = v;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int v) {
        score = v;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int v) {
        totalSteps = v;
    }

    public int getCorrectSteps() {
        return correctSteps;
    }

    public void setCorrectSteps(int v) {
        correctSteps = v;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(long v) {
        timeElapsed = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public List<AttemptErrorEmbeddable> getErrors() {
        return errors;
    }

    public void setErrors(List<AttemptErrorEmbeddable> v) {
        errors = v;
    }

    @Embeddable
    public static class AttemptErrorEmbeddable {
        private int stepNumber;
        @Column(length = 1000)
        private String description;
        private String severity;

        public int getStepNumber() {
            return stepNumber;
        }

        public void setStepNumber(int v) {
            stepNumber = v;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String v) {
            description = v;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String v) {
            severity = v;
        }
    }
}
