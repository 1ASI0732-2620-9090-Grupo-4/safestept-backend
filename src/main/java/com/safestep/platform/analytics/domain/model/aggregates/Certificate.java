package com.safestep.platform.analytics.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.time.Instant;

public class Certificate extends AbstractDomainAggregateRoot<Certificate> {
    private Long id;
    private String username;
    private String moduleName;
    private int score;
    private String achievementLevel;
    private Instant issuedAt;
    private String verificationCode;
    private String qrCodeUrl;
    private String downloadablePdfUrl;

    public Certificate() {}

    public Certificate(Long id, String username, String moduleName, int score, String achievementLevel,
            Instant issuedAt, String verificationCode, String qrCodeUrl, String downloadablePdfUrl) {
        this.id = id;
        this.username = username;
        this.moduleName = moduleName;
        this.score = score;
        this.achievementLevel = achievementLevel;
        this.issuedAt = issuedAt;
        this.verificationCode = verificationCode;
        this.qrCodeUrl = qrCodeUrl;
        this.downloadablePdfUrl = downloadablePdfUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public String getModuleName() { return moduleName; }
    public int getScore() { return score; }
    public String getAchievementLevel() { return achievementLevel; }
    public Instant getIssuedAt() { return issuedAt; }
    public String getVerificationCode() { return verificationCode; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public String getDownloadablePdfUrl() { return downloadablePdfUrl; }
}
