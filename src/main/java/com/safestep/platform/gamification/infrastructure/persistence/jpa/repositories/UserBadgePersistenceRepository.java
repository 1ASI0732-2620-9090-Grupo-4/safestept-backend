package com.safestep.platform.gamification.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.gamification.infrastructure.persistence.jpa.entities.UserBadgePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserBadgePersistenceRepository extends JpaRepository<UserBadgePersistenceEntity, Long> {
    boolean existsByUsernameAndBadgeExternalId(String username, String badgeId);

    List<UserBadgePersistenceEntity> findByUsername(String username);
}
