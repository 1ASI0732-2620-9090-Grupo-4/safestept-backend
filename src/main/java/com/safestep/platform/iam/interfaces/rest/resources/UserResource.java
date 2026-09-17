package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Resource representing an IAM user returned by the REST API.
 */
@Schema(name = "UserResponse", description = "User information response", example = "{\"id\": 1, \"username\": \"john.doe\", \"roles\": [\"ROLE_USER\", \"ROLE_INSTRUCTOR\"], \"enabled\": true, \"accountNonLocked\": true, \"accountNonExpired\": true, \"credentialsNonExpired\": true}")
public record UserResource(@Schema(description = "User unique identifier", example = "1") Long id,

        @Schema(description = "User username", example = "john.doe") String username,

        @Schema(description = "User assigned roles", example = "[\"ROLE_USER\"]") List<String> roles,

        @Schema(description = "Whether the user can authenticate", example = "true") boolean enabled,

        @Schema(description = "Whether the user account is not locked", example = "true") boolean accountNonLocked,

        @Schema(description = "Whether the user account is not expired", example = "true") boolean accountNonExpired,

        @Schema(description = "Whether the user credentials are not expired", example = "true") boolean credentialsNonExpired) {
}
