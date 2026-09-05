package com.safestep.platform.iam.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
public class PasswordResetToken extends AbstractDomainAggregateRoot<PasswordResetToken> {
    @Setter
    private Long id;
    private final String tokenHash;
    private final String username;
    private final Instant expiresAt;
    private Instant usedAt;

    public PasswordResetToken(Long id, String tokenHash, String username, Instant expiresAt, Instant usedAt) {
        this.id = id;
        this.tokenHash = tokenHash;
        this.username = username;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isActive(Instant now) {
        return !isUsed() && !isExpired(now);
    }

    public void markUsed(Instant usedAt) {
        this.usedAt = usedAt;
    }
}
