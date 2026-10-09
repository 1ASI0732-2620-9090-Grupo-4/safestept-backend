package com.safestep.platform.simulation.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import com.safestep.platform.simulation.domain.model.aggregates.MedicalSimulation;
import com.safestep.platform.simulation.domain.model.aggregates.SimulationAttempt;
import com.safestep.platform.simulation.domain.model.commands.CreateSimulationAttemptCommand;
import com.safestep.platform.simulation.domain.model.commands.CreateSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.DeleteSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.UpdateSimulationCommand;
import com.safestep.platform.simulation.domain.model.entities.AttemptError;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.AttemptMode;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.AttemptStatus;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.Difficulty;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.SimulationReward;
import com.safestep.platform.simulation.domain.repositories.MedicalSimulationRepository;
import com.safestep.platform.simulation.domain.repositories.SimulationAttemptRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SimulationAttemptCommandServiceImplTest {

    private MedicalSimulationRepository simulations;
    private SimulationAttemptRepository attempts;
    private SimulationAttemptCommandServiceImpl attemptService;
    private SimulationCommandServiceImpl simulationService;

    @BeforeEach
    void setUp() {
        simulations = mock(MedicalSimulationRepository.class);
        attempts = mock(SimulationAttemptRepository.class);
        attemptService = new SimulationAttemptCommandServiceImpl(simulations, attempts);
        simulationService = new SimulationCommandServiceImpl(simulations);
    }

    private MedicalSimulation simulation(String slug) {
        return new MedicalSimulation(5L, slug, "Adult CPR", "Cardiac arrest", Difficulty.BASIC, 10, "Desc", "img.png",
                "Disponible", 0, new SimulationReward(120, 30), List.of(), List.of(), List.of());
    }

    private ApplicationError errorOf(Result<?, ApplicationError> result) {
        return ((Result.Failure<?, ApplicationError>) result).error();
    }

    @Test
    @DisplayName("handle(CreateSimulationAttemptCommand) should fail for an unknown simulation (AAA)")
    void attempt_UnknownSimulation_ReturnsNotFound() {
        // Arrange
        when(simulations.findBySlug("ghost")).thenReturn(Optional.empty());

        // Act
        var result = attemptService.handle(new CreateSimulationAttemptCommand("ghost", "ana", "practice",
                Instant.now(), null, 80, 10, 8, 120, List.of()));

        // Assert
        assertEquals("SIMULATION_NOT_FOUND", errorOf(result).code());
        verify(attempts, never()).save(any());
    }

    @Test
    @DisplayName("handle(CreateSimulationAttemptCommand) should store a completed attempt with its errors (AAA)")
    void attempt_ValidCommand_StoresCompletedAttempt() {
        // Arrange
        when(simulations.findBySlug("cpr")).thenReturn(Optional.of(simulation("cpr")));
        when(attempts.save(any(SimulationAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = attemptService.handle(new CreateSimulationAttemptCommand("cpr", "ana", "evaluation", null, null,
                85, 10, 8, 300, List.of(new AttemptError(3, "Wrong rhythm", "HIGH"))));

        // Assert
        var attempt = result.toOptional().orElseThrow();
        assertEquals("ana", attempt.getUsername());
        assertEquals(AttemptMode.EVALUATION, attempt.getMode());
        assertEquals(AttemptStatus.COMPLETED, attempt.getStatus());
        assertNotNull(attempt.getStartedAt(), "A missing start date must default to now");
        assertNotNull(attempt.getCompletedAt(), "A completed attempt must have a completion date");
        assertEquals(1, attempt.getErrors().size());
        assertTrue(attempt.getExternalId().startsWith("attempt-"));
    }

    @Test
    @DisplayName("handle(CreateSimulationCommand) should reject duplicated slugs and save new simulations (AAA)")
    void createSimulation_DuplicateAndNew() {
        // Arrange
        var fresh = simulation("fresh");
        var duplicate = simulation("dup");
        when(simulations.existsBySlug("fresh")).thenReturn(false);
        when(simulations.existsBySlug("dup")).thenReturn(true);
        when(simulations.save(fresh)).thenReturn(fresh);

        // Act + Assert
        assertTrue(simulationService.handle(new CreateSimulationCommand(fresh)).isSuccess());
        assertEquals("SIMULATION_CONFLICT", errorOf(simulationService.handle(new CreateSimulationCommand(duplicate))).code());
    }

    @Test
    @DisplayName("handle(UpdateSimulationCommand) should keep the stored id and reject blank or unknown ids (AAA)")
    void updateSimulation_KeepsStoredId() {
        // Arrange
        when(simulations.findBySlug("cpr")).thenReturn(Optional.of(simulation("cpr")));
        when(simulations.findBySlug("ghost")).thenReturn(Optional.empty());
        when(simulations.save(any(MedicalSimulation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var updated = simulationService.handle(new UpdateSimulationCommand("cpr", simulation("ignored")));
        var blank = simulationService.handle(new UpdateSimulationCommand(" ", simulation("x")));
        var missing = simulationService.handle(new UpdateSimulationCommand("ghost", simulation("x")));

        // Assert
        assertEquals(5L, updated.toOptional().orElseThrow().getId());
        assertEquals("cpr", updated.toOptional().orElseThrow().getSlug());
        assertEquals("VALIDATION_ERROR", errorOf(blank).code());
        assertEquals("SIMULATION_NOT_FOUND", errorOf(missing).code());
    }

    @Test
    @DisplayName("handle(DeleteSimulationCommand) should delete existing simulations only (AAA)")
    void deleteSimulation_ExistingAndUnknown() {
        // Arrange
        var existing = simulation("cpr");
        when(simulations.findBySlug("cpr")).thenReturn(Optional.of(existing));
        when(simulations.findBySlug("ghost")).thenReturn(Optional.empty());

        // Act
        var deleted = simulationService.handle(new DeleteSimulationCommand("cpr"));
        var missing = simulationService.handle(new DeleteSimulationCommand("ghost"));

        // Assert
        assertEquals("Simulation deleted", deleted.getOrElse(""));
        assertEquals("SIMULATION_NOT_FOUND", errorOf(missing).code());
        verify(simulations).delete(existing);
    }
}
