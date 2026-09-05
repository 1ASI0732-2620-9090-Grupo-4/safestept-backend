package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Resource received to register a new IAM user.
 */
@Schema(name = "SignUpRequest", description = "Public user sign-up request. Public registration always assigns ROLE_USER.", example = "{\"username\": \"john.doe\", \"password\": \"SecurePass123!\"}")
public record SignUpResource(
        @NotBlank(message = "{validation.not-blank}") @Size(min = 3, max = 50)
        @Schema(description = "Desired username", example = "john.doe", minLength = 3, maxLength = 50) String username,

        @NotBlank(message = "{validation.not-blank}") @Size(min = 8, max = 255)
        @Schema(description = "User password (minimum 8 characters)", example = "SecurePass123!", minLength = 8, maxLength = 255) String password,

        @Size(max = 3)
        @Schema(hidden = true, description = "Ignored in public registration; ROLE_USER is always assigned") List<String> roles) {
}
