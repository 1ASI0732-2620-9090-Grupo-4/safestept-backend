package com.safestep.platform.analytics.infrastructure.persistence.jpa.seed;

import com.safestep.platform.analytics.domain.model.aggregates.Certificate;
import com.safestep.platform.analytics.domain.repositories.CertificateRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.*;
import java.time.*;

@Component
public class AnalyticsSeedEventHandler {
    private final ObjectMapper mapper;
    private final CertificateRepository repository;

    public AnalyticsSeedEventHandler(ObjectMapper m, CertificateRepository r) {
        mapper = m;
        repository = r;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() throws Exception {
        try (var in = new ClassPathResource("safestep-seed.json").getInputStream()) {
            for (var n : mapper.readTree(in).path("statistics").path("certificates")) {
                var code = n.path("verificationCode").asText();
                if (!repository.existsByVerificationCode(code))
                    repository.save(new Certificate(null, "backend.check", n.path("moduleName").asText(),
                            n.path("score").asInt(), n.path("achievementLevel").asText(),
                            Instant.parse(n.path("issuedAt").asText()), code, n.path("qrCodeUrl").asText(),
                            n.path("downloadablePdfUrl").asText()));
            }
        }
    }
}
