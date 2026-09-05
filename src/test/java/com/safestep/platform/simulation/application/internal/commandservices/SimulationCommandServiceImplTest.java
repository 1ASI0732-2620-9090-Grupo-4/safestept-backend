package com.safestep.platform.simulation.application.internal.commandservices;

import com.safestep.platform.simulation.domain.model.aggregates.MedicalSimulation;
import com.safestep.platform.simulation.domain.model.commands.CreateSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.DeleteSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.UpdateSimulationCommand;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.Difficulty;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.SimulationReward;
import com.safestep.platform.simulation.domain.repositories.MedicalSimulationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulationCommandServiceImplTest {

    @Test
    void createSimulationRejectsDuplicatedSlug() {
        var simulations = mock(MedicalSimulationRepository.class);
        var service = new SimulationCommandServiceImpl(simulations);
        when(simulations.existsBySlug("burns")).thenReturn(true);

        var result = service.handle(new CreateSimulationCommand(simulation("burns", "Burns")));

        assertTrue(result.isFailure());
        verify(simulations, never()).save(any());
    }

    @Test
    void updateSimulationKeepsDatabaseIdAndPathSlug() {
        var simulations = mock(MedicalSimulationRepository.class);
        var service = new SimulationCommandServiceImpl(simulations);
        when(simulations.findBySlug("burns")).thenReturn(Optional.of(simulationWithId(3L, "burns", "Old")));
        when(simulations.save(any(MedicalSimulation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new UpdateSimulationCommand("burns", simulation("ignored", "Updated Burns")));

        assertTrue(result.isSuccess());
        var saved = ArgumentCaptor.forClass(MedicalSimulation.class);
        verify(simulations).save(saved.capture());
        assertEquals(3L, saved.getValue().getId());
        assertEquals("burns", saved.getValue().getSlug());
        assertEquals("Updated Burns", saved.getValue().getTitle());
    }

    @Test
    void deleteSimulationReturnsNotFoundWhenMissing() {
        var simulations = mock(MedicalSimulationRepository.class);
        var service = new SimulationCommandServiceImpl(simulations);
        when(simulations.findBySlug("missing")).thenReturn(Optional.empty());

        var result = service.handle(new DeleteSimulationCommand("missing"));

        assertTrue(result.isFailure());
        verify(simulations, never()).delete(any());
    }

    private MedicalSimulation simulation(String slug, String title) {
        return simulationWithId(null, slug, title);
    }

    private MedicalSimulation simulationWithId(Long id, String slug, String title) {
        return new MedicalSimulation(id, slug, title, "First aid", Difficulty.BASIC, 5,
                "Practice emergency response", "image.png", "active", 0, new SimulationReward(50, 15),
                List.of("Recognize risk"), List.of(), List.of());
    }
}
