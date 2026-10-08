package com.safestep.platform.simulation.interfaces.rest.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.safestep.platform.simulation.domain.model.aggregates.MedicalSimulation;
import com.safestep.platform.simulation.domain.model.aggregates.SimulationAttempt;
import com.safestep.platform.simulation.domain.model.entities.AttemptError;
import com.safestep.platform.simulation.domain.model.entities.SimulationStep;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.AttemptMode;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.AttemptStatus;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.Difficulty;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.SimulationReward;
import com.safestep.platform.simulation.interfaces.rest.resources.AttemptErrorResource;
import com.safestep.platform.simulation.interfaces.rest.resources.CreateAttemptResource;
import com.safestep.platform.simulation.interfaces.rest.resources.OptionResource;
import com.safestep.platform.simulation.interfaces.rest.resources.ProductSuggestionResource;
import com.safestep.platform.simulation.interfaces.rest.resources.SimulationResource;
import com.safestep.platform.simulation.interfaces.rest.resources.StepResource;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SimulationResourceAssemblerTest {

    private static final Instant START = Instant.parse("2026-09-14T15:00:00Z");
    private static final Instant END = Instant.parse("2026-09-14T15:05:00Z");

    private MedicalSimulation simulation() {
        return new MedicalSimulation(1L, "cpr-adult", "Adult CPR", "Cardiac arrest", Difficulty.INTERMEDIATE, 10,
                "Practice chest compressions", "cpr.png", "Disponible", 40, new SimulationReward(120, 30),
                List.of("Call emergency services"),
                List.of(new SimulationStep("s1", "What do you do first?", "o1",
                        List.of(new SimulationStep.SimulationOption("o1", "Call 911", "Correct"),
                                new SimulationStep.SimulationOption("o2", "Wait", "Wrong")))),
                List.of(new MedicalSimulation.ProductSuggestion("prod-1", "Practice mannequin")));
    }

    @Test
    @DisplayName("toResource(MedicalSimulation) should map every field including steps and suggestions (AAA)")
    void toResource_MedicalSimulation_MapsAllFields() {
        // Arrange
        var simulation = simulation();

        // Act
        var resource = SimulationResourceAssembler.toResource(simulation);

        // Assert
        assertEquals("cpr-adult", resource.id());
        assertEquals("INTERMEDIATE", resource.difficulty());
        assertEquals(120, resource.xpReward());
        assertEquals(30, resource.coinReward());
        assertEquals(1, resource.steps().size());
        assertEquals("o1", resource.steps().get(0).correctOptionId());
        assertEquals(2, resource.steps().get(0).options().size());
        assertEquals("prod-1", resource.productSuggestions().get(0).productId());
    }

    @Test
    @DisplayName("toSimulation(SimulationResource) should rebuild the aggregate from the resource (AAA)")
    void toSimulation_RebuildsAggregate() {
        // Arrange
        var resource = new SimulationResource("fire-basic", "Fire", "Burns", "Avanzado", 8, 90, "fire.png",
                "Disponible", 0, "Handle a small fire", List.of("Stay calm"),
                List.of(new StepResource("s1", "Prompt", "o1", List.of(new OptionResource("o1", "Label", "FB")))),
                List.of(new ProductSuggestionResource("prod-9", "Extinguisher")), 20);

        // Act
        var simulation = SimulationResourceAssembler.toSimulation(resource);

        // Assert
        assertEquals("fire-basic", simulation.getSlug());
        assertEquals(Difficulty.ADVANCED, simulation.getDifficulty());
        assertEquals(90, simulation.getReward().xp());
        assertEquals(20, simulation.getReward().coins());
        assertEquals("o1", simulation.getSteps().get(0).options().get(0).externalId());
        assertEquals("prod-9", simulation.getProductSuggestions().get(0).productId());
    }

    @Test
    @DisplayName("toSimulation(SimulationResource) should tolerate null collections (AAA)")
    void toSimulation_WithNullCollections_UsesEmptyLists() {
        // Arrange
        var resource = new SimulationResource("basic", "Basic", "General", "Basic", 5, 10, null, "Disponible", 0,
                "Desc", null, null, null, 0);

        // Act
        var simulation = SimulationResourceAssembler.toSimulation(resource);

        // Assert
        assertTrue(simulation.getSteps().isEmpty());
        assertTrue(simulation.getProductSuggestions().isEmpty());
        assertTrue(simulation.getLearningGoals().isEmpty());
    }

    @Test
    @DisplayName("toCommand should map the attempt resource and its errors to a command (AAA)")
    void toCommand_MapsAttemptResource() {
        // Arrange
        var resource = new CreateAttemptResource("evaluation", START, END, 85, 10, 8, 300,
                List.of(new AttemptErrorResource(3, "Wrong rhythm", "HIGH")));

        // Act
        var command = SimulationResourceAssembler.toCommand("cpr-adult", "ana", resource);

        // Assert
        assertEquals("cpr-adult", command.simulationSlug());
        assertEquals("ana", command.username());
        assertEquals("evaluation", command.mode());
        assertEquals(85, command.score());
        assertEquals(1, command.errors().size());
        assertEquals("Wrong rhythm", command.errors().get(0).description());
    }

    @Test
    @DisplayName("toCommand should use an empty error list when the resource has none (AAA)")
    void toCommand_WithoutErrors_UsesEmptyList() {
        // Arrange
        var resource = new CreateAttemptResource("practice", START, null, 50, 4, 2, 60, null);

        // Act
        var command = SimulationResourceAssembler.toCommand("cpr-adult", "ana", resource);

        // Assert
        assertTrue(command.errors().isEmpty());
    }

    @Test
    @DisplayName("toResource(SimulationAttempt) should expose the attempt with lowercase mode (AAA)")
    void toResource_SimulationAttempt_MapsAttempt() {
        // Arrange
        var attempt = new SimulationAttempt(1L, "att-1", "ana", "cpr-adult", AttemptMode.EVALUATION, START, END, 85,
                10, 8, 300, AttemptStatus.COMPLETED, List.of(new AttemptError(3, "Wrong rhythm", "HIGH")));

        // Act
        var resource = SimulationResourceAssembler.toResource(attempt);

        // Assert
        assertEquals("att-1", resource.id());
        assertEquals("ana", resource.userId());
        assertEquals("evaluation", resource.mode());
        assertEquals(85, resource.score());
        assertEquals("cpr-adult", resource.scenarioSlug());
        assertEquals(1, resource.errors().size());
        assertEquals(3, resource.errors().get(0).stepNumber());
    }
}
