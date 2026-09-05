package com.safestep.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.safestep.platform.iam.domain.model.aggregates.RefreshToken;
import com.safestep.platform.iam.infrastructure.persistence.jpa.entities.RefreshTokenPersistenceEntity;

public final class RefreshTokenPersistenceAssembler {
    private RefreshTokenPersistenceAssembler() {
    }

    public static RefreshToken toDomainFromPersistence(RefreshTokenPersistenceEntity entity) {
        return new RefreshToken(entity.getId(), entity.getTokenHash(), entity.getUsername(), entity.getExpiresAt(),
                entity.getRevokedAt());
    }

    public static RefreshTokenPersistenceEntity toPersistenceFromDomain(RefreshToken token) {
        var entity = new RefreshTokenPersistenceEntity();
        if (token.getId() != null) {
            entity.setId(token.getId());
        }
        entity.setTokenHash(token.getTokenHash());
        entity.setUsername(token.getUsername());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevokedAt(token.getRevokedAt());
        return entity;
    }
}
