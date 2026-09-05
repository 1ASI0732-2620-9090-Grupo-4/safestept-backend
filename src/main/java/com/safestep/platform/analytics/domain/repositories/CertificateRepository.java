package com.safestep.platform.analytics.domain.repositories;

import com.safestep.platform.analytics.domain.model.aggregates.Certificate;
import java.util.*;

public interface CertificateRepository {
    List<Certificate> findByUsername(String username);

    Certificate save(Certificate certificate);

    boolean existsByVerificationCode(String code);
}
