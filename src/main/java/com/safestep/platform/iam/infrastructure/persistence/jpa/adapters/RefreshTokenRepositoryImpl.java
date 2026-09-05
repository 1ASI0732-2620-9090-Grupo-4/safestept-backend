package com.safestep.platform.iam.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.iam.domain.model.aggregates.RefreshToken;
import com.safestep.platform.iam.domain.repositories.RefreshTokenRepository;
import com.safestep.platform.iam.infrastructure.persistence.jpa.assemblers.RefreshTokenPersistenceAssembler;
import com.safestep.platform.iam.infrastructure.persistence.jpa.repositories.RefreshTokenPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    private final RefreshTokenPersistenceRepository refreshTokenPersistenceRepository;

    public RefreshTokenRepositoryImpl(RefreshTokenPersistenceRepository refreshTokenPersistenceRepository) {
        this.refreshTokenPersistenceRepository = refreshTokenPersistenceRepository;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenPersistenceRepository.findByTokenHash(tokenHash)
                .map(RefreshTokenPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<RefreshToken> findActiveByUsername(String username) {
        return refreshTokenPersistenceRepository.findByUsernameAndRevokedAtIsNull(username).stream()
                .map(RefreshTokenPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        var saved = refreshTokenPersistenceRepository
                .save(RefreshTokenPersistenceAssembler.toPersistenceFromDomain(refreshToken));
        return RefreshTokenPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
