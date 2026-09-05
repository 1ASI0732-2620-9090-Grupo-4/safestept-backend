package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RefreshTokenRequest", description = "Refresh token rotation request")
public record RefreshTokenResource(
        @NotBlank(message = "{validation.not-blank}") @Schema(description = "Refresh token received during sign-in") String refreshToken) {
}
