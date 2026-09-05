package com.safestep.platform.gamification.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "user_mission_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "username",
        "mission_external_id" }))
public class UserMissionProgressPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false)
    private String username;
    @Column(name = "mission_external_id", nullable = false)
    private String missionExternalId;
    private int progress;
    private boolean completed;

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        username = v;
    }

    public String getMissionExternalId() {
        return missionExternalId;
    }

    public void setMissionExternalId(String v) {
        missionExternalId = v;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int v) {
        progress = v;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean v) {
        completed = v;
    }
}
