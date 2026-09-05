package com.safestep.platform.analytics.application.internal.eventhandlers;

import com.safestep.platform.analytics.application.commandservices.CertificateCommandService;
import com.safestep.platform.simulation.interfaces.events.SimulationAttemptCompletedIntegrationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CertificateOnAttemptCompletedEventHandler {
    private final CertificateCommandService certificates;

    public CertificateOnAttemptCompletedEventHandler(CertificateCommandService c) {
        certificates = c;
    }

    @EventListener
    public void on(SimulationAttemptCompletedIntegrationEvent e) {
        if (e.score() >= 80)
            certificates.issue(e.username(), e.simulationSlug(), e.simulationTitle(), e.score());
    }
}
