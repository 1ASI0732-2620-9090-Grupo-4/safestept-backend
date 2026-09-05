package com.safestep.platform.iam.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
public class RefreshToken extends AbstractDomainAggregateRoot<RefreshToken> {
    @Setter
    private Long id;
    private final String tokenHash;
    private final String username;
    private final Instant expiresAt;
    private Instant revokedAt;

    public RefreshToken(Long id, String tokenHash, String username, Instant expiresAt, Instant revokedAt) {
        this.id = id;
        this.tokenHash = tokenHash;
        this.username = username;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive(Instant now) {
        return !isRevoked() && !isExpired(now);
    }

    public void revoke(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
