package com.safestep.platform.analytics.application.commandservices;

import com.safestep.platform.analytics.domain.model.aggregates.Certificate;
import com.safestep.platform.shared.application.result.*;

public interface CertificateCommandService {
    Result<Certificate, ApplicationError> issue(String username, String simulationSlug, String simulationTitle,
            int score);
}
