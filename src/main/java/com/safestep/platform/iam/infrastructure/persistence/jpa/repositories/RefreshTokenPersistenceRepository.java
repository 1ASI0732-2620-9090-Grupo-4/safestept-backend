package com.safestep.platform.iam.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.iam.infrastructure.persistence.jpa.entities.RefreshTokenPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenPersistenceRepository extends JpaRepository<RefreshTokenPersistenceEntity, Long> {
    Optional<RefreshTokenPersistenceEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenPersistenceEntity> findByUsernameAndRevokedAtIsNull(String username);
}
