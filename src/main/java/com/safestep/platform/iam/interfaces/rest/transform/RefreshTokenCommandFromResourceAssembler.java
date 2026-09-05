package com.safestep.platform.iam.interfaces.rest.transform;

import com.safestep.platform.iam.domain.model.commands.RefreshTokenCommand;
import com.safestep.platform.iam.interfaces.rest.resources.RefreshTokenResource;

public final class RefreshTokenCommandFromResourceAssembler {
    private RefreshTokenCommandFromResourceAssembler() {
    }

    public static RefreshTokenCommand toCommandFromResource(RefreshTokenResource resource) {
        return new RefreshTokenCommand(resource.refreshToken());
    }
}
