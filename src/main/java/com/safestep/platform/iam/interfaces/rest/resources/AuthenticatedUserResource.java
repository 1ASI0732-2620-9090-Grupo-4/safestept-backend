package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Resource returned after successful authentication.
 * <p>
 * Contains the authenticated user identifier, username, and the bearer token to be used in subsequent API calls.
 * </p>
 */
@Schema(name = "AuthenticatedUserResponse", description = "Authenticated user information with access and refresh tokens", example = "{\"id\": 1, \"username\": \"john.doe\", \"token\": \"eyJhbGciOiJIUzI1NiIs...\", \"refreshToken\": \"refresh-token-value\"}")
public record AuthenticatedUserResource(@Schema(description = "User unique identifier", example = "1") Long id,

        @Schema(description = "User username", example = "john.doe") String username,

        @Schema(description = "JWT Bearer access token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String token,

        @Schema(description = "Refresh token used to rotate authentication tokens") String refreshToken,

        @Schema(description = "Authorities granted to this user", example = "[\"ROLE_USER\"]") List<String> roles) {
}
