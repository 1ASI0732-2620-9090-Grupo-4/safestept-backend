package com.safestep.platform.analytics.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "certificates")
public class CertificatePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false)
    private String username;
    private String moduleName, achievementLevel;
    private int score;
    private Instant issuedAt;
    @Column(nullable = false, unique = true)
    private String verificationCode;
    private String qrCodeUrl, downloadablePdfUrl;

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        username = v;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String v) {
        moduleName = v;
    }

    public String getAchievementLevel() {
        return achievementLevel;
    }

    public void setAchievementLevel(String v) {
        achievementLevel = v;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int v) {
        score = v;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant v) {
        issuedAt = v;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String v) {
        verificationCode = v;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String v) {
        qrCodeUrl = v;
    }

    public String getDownloadablePdfUrl() {
        return downloadablePdfUrl;
    }

    public void setDownloadablePdfUrl(String v) {
        downloadablePdfUrl = v;
    }
}
