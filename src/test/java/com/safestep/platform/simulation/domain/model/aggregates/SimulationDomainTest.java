package com.safestep.platform.simulation.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.safestep.platform.simulation.domain.model.events.SimulationAttemptCompletedEvent;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.AttemptMode;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.AttemptStatus;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.Difficulty;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.Score;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.SimulationReward;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.SimulationSlug;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SimulationDomainTest {

    private MedicalSimulation simulation() {
        return new MedicalSimulation(1L, "cpr-adult", "Adult CPR", "Cardiac arrest", Difficulty.BASIC, 10, "Desc",
                "img.png", "Disponible", 0, new SimulationReward(120, 30), List.of(), List.of(), List.of());
    }

    @Test
    @DisplayName("markCompleted should publish a completed event carrying the simulation reward (AAA)")
    void markCompleted_RegistersCompletionEvent() {
        // Arrange
        var attempt = new SimulationAttempt(null, "att-1", "ana", "cpr-adult", AttemptMode.PRACTICE, Instant.now(),
                null, 80, 10, 8, 120, AttemptStatus.IN_PROGRESS, List.of());

        // Act
        attempt.markCompleted(simulation());

        // Assert
        assertEquals(AttemptStatus.COMPLETED, attempt.getStatus());
        assertNotNull(attempt.getCompletedAt());
        var event = (SimulationAttemptCompletedEvent) attempt.domainEvents().iterator().next();
        assertEquals("att-1", event.attemptId());
        assertEquals(120, event.xpReward());
        assertEquals(30, event.coinReward());
        assertEquals(0.8, event.accuracy());
    }

    @Test
    @DisplayName("markCompleted should derive accuracy from the score when there are no steps (AAA)")
    void markCompleted_WithoutSteps_UsesScoreAsAccuracy() {
        // Arrange
        var attempt = new SimulationAttempt(null, "att-2", "ana", "cpr-adult", AttemptMode.GUIDED, Instant.now(),
                Instant.now(), 60, 0, 0, 30, AttemptStatus.IN_PROGRESS, null);

        // Act
        attempt.markCompleted(simulation());

        // Assert
        var event = (SimulationAttemptCompletedEvent) attempt.domainEvents().iterator().next();
        assertEquals(0.6, event.accuracy());
        assertTrue(attempt.getErrors().isEmpty());
    }

    @Test
    @DisplayName("value objects should validate scores, slugs and rewards (AAA)")
    void valueObjects_ValidateInputs() {
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new Score(101));
        assertThrows(IllegalArgumentException.class, () -> new Score(-1));
        assertThrows(IllegalArgumentException.class, () -> new SimulationSlug(" "));
        assertThrows(IllegalArgumentException.class, () -> new SimulationReward(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SimulationReward(0, -1));
    }

    @Test
    @DisplayName("lenient enums should understand Spanish labels and fall back to defaults (AAA)")
    void enums_UnderstandLabelsAndDefaults() {
        // Assert
        assertEquals(Difficulty.BASIC, Difficulty.from(null));
        assertEquals(Difficulty.INTERMEDIATE, Difficulty.from("Intermedio"));
        assertEquals(Difficulty.ADVANCED, Difficulty.from("Avanzado"));
        assertEquals(Difficulty.ADVANCED, Difficulty.from("advanced"));
        assertEquals(Difficulty.BASIC, Difficulty.from("whatever"));
        assertEquals(AttemptMode.PRACTICE, AttemptMode.from(null));
        assertEquals(AttemptMode.EVALUATION, AttemptMode.from("evaluation"));
        assertEquals(AttemptMode.PRACTICE, AttemptMode.from("unknown"));
    }

    @Test
    @DisplayName("MedicalSimulation should default missing collections to empty lists (AAA)")
    void medicalSimulation_DefaultsCollections() {
        // Act
        var simulation = new MedicalSimulation(2L, "burns", "Burns", "Burns", Difficulty.BASIC, 5, "d", "i", "s", 0,
                new SimulationReward(1, 1), null, null, null);

        // Assert
        assertTrue(simulation.getLearningGoals().isEmpty());
        assertTrue(simulation.getSteps().isEmpty());
        assertTrue(simulation.getProductSuggestions().isEmpty());
        assertEquals("burns", simulation.getSlug());
    }
}
