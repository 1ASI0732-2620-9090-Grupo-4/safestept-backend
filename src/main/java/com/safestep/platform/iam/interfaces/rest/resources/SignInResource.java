package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Resource received to authenticate an existing user.
 */
@Schema(name = "SignInRequest", description = "User sign-in request with credentials", example = "{\"username\": \"john.doe\", \"password\": \"SecurePass123!\"}")
public record SignInResource(
        @NotBlank(message = "{validation.not-blank}") @Size(min = 3, max = 50)
        @Schema(description = "Username", example = "john.doe", minLength = 3, maxLength = 50) String username,

        @NotBlank(message = "{validation.not-blank}") @Size(min = 8, max = 255)
        @Schema(description = "User password", example = "SecurePass123!", minLength = 8, maxLength = 255) String password) {
}
