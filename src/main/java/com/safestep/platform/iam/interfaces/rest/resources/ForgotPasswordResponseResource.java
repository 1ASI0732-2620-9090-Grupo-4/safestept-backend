package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "ForgotPasswordResponse", description = "Local/dev password reset token response")
public record ForgotPasswordResponseResource(
        @Schema(description = "Username that requested the reset", example = "john.doe") String username,

        @Schema(description = "Temporary reset token returned only in local/dev mode") String resetToken,

        @Schema(description = "Reset token expiration timestamp") Instant expiresAt,

        @Schema(description = "Delivery mode", example = "LOCAL_DEV_RESPONSE") String mode) {
}
