package com.safestep.platform.gamification.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "user_badges", uniqueConstraints = @UniqueConstraint(columnNames = { "username", "badge_external_id" }))
public class UserBadgePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false)
    private String username;
    @Column(name = "badge_external_id", nullable = false)
    private String badgeExternalId;
    private Instant unlockedAt;

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        username = v;
    }

    public String getBadgeExternalId() {
        return badgeExternalId;
    }

    public void setBadgeExternalId(String v) {
        badgeExternalId = v;
    }

    public Instant getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(Instant v) {
        unlockedAt = v;
    }
}
