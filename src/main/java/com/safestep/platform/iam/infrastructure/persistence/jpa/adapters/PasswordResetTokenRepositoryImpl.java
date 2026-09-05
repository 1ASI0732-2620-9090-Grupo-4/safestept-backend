package com.safestep.platform.iam.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.iam.domain.model.aggregates.PasswordResetToken;
import com.safestep.platform.iam.domain.repositories.PasswordResetTokenRepository;
import com.safestep.platform.iam.infrastructure.persistence.jpa.assemblers.PasswordResetTokenPersistenceAssembler;
import com.safestep.platform.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {
    private final PasswordResetTokenPersistenceRepository passwordResetTokenPersistenceRepository;

    public PasswordResetTokenRepositoryImpl(PasswordResetTokenPersistenceRepository passwordResetTokenPersistenceRepository) {
        this.passwordResetTokenPersistenceRepository = passwordResetTokenPersistenceRepository;
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return passwordResetTokenPersistenceRepository.findByTokenHash(tokenHash)
                .map(PasswordResetTokenPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken passwordResetToken) {
        var saved = passwordResetTokenPersistenceRepository
                .save(PasswordResetTokenPersistenceAssembler.toPersistenceFromDomain(passwordResetToken));
        return PasswordResetTokenPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
