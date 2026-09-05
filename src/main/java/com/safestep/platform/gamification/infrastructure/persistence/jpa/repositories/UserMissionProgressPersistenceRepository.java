package com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.gamification.infrastructure.persistence.jpa.entities.UserMissionProgressPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserMissionProgressPersistenceRepository
        extends JpaRepository<UserMissionProgressPersistenceEntity, Long> {
    Optional<UserMissionProgressPersistenceEntity> findByUsernameAndMissionExternalId(String username,
            String missionId);
}
