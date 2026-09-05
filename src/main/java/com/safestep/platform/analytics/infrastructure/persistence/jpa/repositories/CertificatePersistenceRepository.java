package com.safestep.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.analytics.infrastructure.persistence.jpa.entities.CertificatePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CertificatePersistenceRepository extends JpaRepository<CertificatePersistenceEntity, Long> {
    List<CertificatePersistenceEntity> findByUsernameOrderByIssuedAtDesc(String username);

    boolean existsByVerificationCode(String code);
}
