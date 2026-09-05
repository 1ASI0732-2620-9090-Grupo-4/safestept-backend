package com.safestep.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.safestep.platform.iam.domain.model.aggregates.PasswordResetToken;
import com.safestep.platform.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenPersistenceEntity;

public final class PasswordResetTokenPersistenceAssembler {
    private PasswordResetTokenPersistenceAssembler() {
    }

    public static PasswordResetToken toDomainFromPersistence(PasswordResetTokenPersistenceEntity entity) {
        return new PasswordResetToken(entity.getId(), entity.getTokenHash(), entity.getUsername(),
                entity.getExpiresAt(), entity.getUsedAt());
    }

    public static PasswordResetTokenPersistenceEntity toPersistenceFromDomain(PasswordResetToken token) {
        var entity = new PasswordResetTokenPersistenceEntity();
        if (token.getId() != null) {
            entity.setId(token.getId());
        }
        entity.setTokenHash(token.getTokenHash());
        entity.setUsername(token.getUsername());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsedAt(token.getUsedAt());
        return entity;
    }
}
