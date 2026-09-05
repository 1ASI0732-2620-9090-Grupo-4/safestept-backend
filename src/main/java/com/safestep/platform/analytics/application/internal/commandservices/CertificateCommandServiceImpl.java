package com.safestep.platform.analytics.application.internal.commandservices;

import com.safestep.platform.analytics.application.commandservices.CertificateCommandService;
import com.safestep.platform.analytics.domain.model.aggregates.Certificate;
import com.safestep.platform.analytics.domain.repositories.CertificateRepository;
import com.safestep.platform.shared.application.result.*;
import org.springframework.stereotype.Service;
import java.time.*;

@Service
public class CertificateCommandServiceImpl implements CertificateCommandService {
    private final CertificateRepository repository;

    public CertificateCommandServiceImpl(CertificateRepository r) {
        repository = r;
    }

    @Override
    public Result<Certificate, ApplicationError> issue(String u, String slug, String title, int score) {
        if (score < 80)
            return Result.failure(
                    ApplicationError.businessRuleViolation("certificate", "A score of at least 80 is required"));
        var code = ("CERT-SS-" + u + "-" + slug).toUpperCase().replaceAll("[^A-Z0-9-]", "-");
        if (repository.existsByVerificationCode(code))
            return Result.failure(ApplicationError.conflict("certificate", "Certificate was already issued"));
        var c = new Certificate(null, u, title, score, score >= 95 ? "Avanzado" : score >= 90 ? "Intermedio" : "Basico",
                Instant.now(), code, "https://safestep.com/verify/" + code,
                "https://safestep.com/certificates/" + code + ".pdf");
        return Result.success(repository.save(c));
    }
}
