package com.safestep.platform.iam.domain.repositories;

import com.safestep.platform.iam.domain.model.aggregates.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findActiveByUsername(String username);

    RefreshToken save(RefreshToken refreshToken);
}
