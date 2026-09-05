package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ForgotPasswordRequest", description = "Local/dev password reset token request")
public record ForgotPasswordResource(
        @NotBlank(message = "{validation.not-blank}") @Size(min = 3, max = 50)
        @Schema(description = "Username requesting password recovery", example = "john.doe") String username) {
}
