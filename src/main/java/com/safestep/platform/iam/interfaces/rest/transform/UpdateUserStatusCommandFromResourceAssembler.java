package com.safestep.platform.iam.interfaces.rest.transform;

import com.safestep.platform.iam.domain.model.commands.UpdateUserStatusCommand;
import com.safestep.platform.iam.interfaces.rest.resources.UpdateUserStatusResource;

public final class UpdateUserStatusCommandFromResourceAssembler {
    private UpdateUserStatusCommandFromResourceAssembler() {
    }

    public static UpdateUserStatusCommand toCommandFromResource(Long userId, UpdateUserStatusResource resource) {
        return new UpdateUserStatusCommand(userId, resource.enabled(), resource.accountNonLocked(),
                resource.accountNonExpired(), resource.credentialsNonExpired());
    }
}
