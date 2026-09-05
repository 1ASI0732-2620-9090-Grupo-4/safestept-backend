package com.safestep.platform.iam.domain.repositories;

import com.safestep.platform.iam.domain.model.aggregates.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    PasswordResetToken save(PasswordResetToken passwordResetToken);
}
