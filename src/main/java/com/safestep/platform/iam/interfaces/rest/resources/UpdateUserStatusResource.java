package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateUserStatusRequest", description = "Admin request to update Spring Security user account flags")
public record UpdateUserStatusResource(
        @Schema(description = "Whether the user can authenticate", example = "true") boolean enabled,

        @Schema(description = "Whether the user account is not locked", example = "true") boolean accountNonLocked,

        @Schema(description = "Whether the user account is not expired", example = "true") boolean accountNonExpired,

        @Schema(description = "Whether the user credentials are not expired", example = "true") boolean credentialsNonExpired) {
}
