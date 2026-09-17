package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(name = "UpdateUserRolesRequest", description = "Admin request to replace a user's assigned roles")
public record UpdateUserRolesResource(
        @NotEmpty @Schema(description = "New set of role names to assign to the user", example = "[\"ROLE_USER\", \"ROLE_INSTRUCTOR\"]") List<String> roles) {
}
