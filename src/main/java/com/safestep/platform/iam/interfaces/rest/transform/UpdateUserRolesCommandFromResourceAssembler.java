package com.safestep.platform.iam.interfaces.rest.transform;

import com.safestep.platform.iam.domain.model.commands.UpdateUserRolesCommand;
import com.safestep.platform.iam.interfaces.rest.resources.UpdateUserRolesResource;

public final class UpdateUserRolesCommandFromResourceAssembler {
    private UpdateUserRolesCommandFromResourceAssembler() {
    }

    public static UpdateUserRolesCommand toCommandFromResource(Long userId, String actingUsername,
            UpdateUserRolesResource resource) {
        return new UpdateUserRolesCommand(userId, resource.roles(), actingUsername);
    }
}
