package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ResetPasswordRequest", description = "Password reset request using a temporary token")
public record ResetPasswordResource(
        @NotBlank(message = "{validation.not-blank}") @Schema(description = "Temporary reset token") String resetToken,

        @NotBlank(message = "{validation.not-blank}") @Size(min = 8, max = 255)
        @Schema(description = "New password", example = "NewSecurePass123!", minLength = 8, maxLength = 255) String newPassword) {
}
