package com.safestep.platform.analytics.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.analytics.domain.model.aggregates.Certificate;
import com.safestep.platform.analytics.domain.repositories.CertificateRepository;
import com.safestep.platform.analytics.infrastructure.persistence.jpa.entities.CertificatePersistenceEntity;
import com.safestep.platform.analytics.infrastructure.persistence.jpa.repositories.CertificatePersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class CertificateRepositoryImpl implements CertificateRepository {
    private final CertificatePersistenceRepository r;

    public CertificateRepositoryImpl(CertificatePersistenceRepository r) {
        this.r = r;
    }

    private Certificate domain(CertificatePersistenceEntity e) {
        return new Certificate(e.getId(), e.getUsername(), e.getModuleName(), e.getScore(), e.getAchievementLevel(),
                e.getIssuedAt(), e.getVerificationCode(), e.getQrCodeUrl(), e.getDownloadablePdfUrl());
    }

    public List<Certificate> findByUsername(String u) {
        return r.findByUsernameOrderByIssuedAtDesc(u).stream().map(this::domain).toList();
    }

    public Certificate save(Certificate d) {
        var e = new CertificatePersistenceEntity();
        e.setId(d.getId());
        e.setUsername(d.getUsername());
        e.setModuleName(d.getModuleName());
        e.setScore(d.getScore());
        e.setAchievementLevel(d.getAchievementLevel());
        e.setIssuedAt(d.getIssuedAt());
        e.setVerificationCode(d.getVerificationCode());
        e.setQrCodeUrl(d.getQrCodeUrl());
        e.setDownloadablePdfUrl(d.getDownloadablePdfUrl());
        return domain(r.save(e));
    }

    public boolean existsByVerificationCode(String c) {
        return r.existsByVerificationCode(c);
    }
}
