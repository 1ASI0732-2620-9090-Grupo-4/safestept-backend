package com.safestep.platform.iam.domain.model.valueobjects;

import java.time.Instant;

public record PasswordResetTokenIssue(String username, String resetToken, Instant expiresAt) {
}
