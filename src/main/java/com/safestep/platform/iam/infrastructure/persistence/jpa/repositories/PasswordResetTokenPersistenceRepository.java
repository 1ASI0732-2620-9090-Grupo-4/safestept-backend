package com.safestep.platform.iam.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenPersistenceRepository extends JpaRepository<PasswordResetTokenPersistenceEntity, Long> {
    Optional<PasswordResetTokenPersistenceEntity> findByTokenHash(String tokenHash);
}
