package com.safestep.platform.simulation.application.internal.eventhandlers;

import com.safestep.platform.simulation.domain.model.events.SimulationAttemptCompletedEvent;
import com.safestep.platform.simulation.interfaces.events.SimulationAttemptCompletedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SimulationAttemptCompletedEventHandler {
    private final ApplicationEventPublisher eventPublisher;

    public SimulationAttemptCompletedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(SimulationAttemptCompletedEvent event) {
        eventPublisher.publishEvent(new SimulationAttemptCompletedIntegrationEvent(event.attemptId(), event.username(),
                event.simulationSlug(), event.simulationTitle(), event.score(), event.accuracy(), event.xpReward(),
                event.coinReward(), event.occurredOn()));
    }
}
