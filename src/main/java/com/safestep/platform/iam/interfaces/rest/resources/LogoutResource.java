package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LogoutRequest", description = "Refresh token revocation request")
public record LogoutResource(
        @NotBlank(message = "{validation.not-blank}") @Schema(description = "Refresh token to revoke") String refreshToken) {
}
