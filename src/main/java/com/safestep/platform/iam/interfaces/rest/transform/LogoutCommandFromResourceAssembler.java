package com.safestep.platform.iam.interfaces.rest.transform;

import com.safestep.platform.iam.domain.model.commands.LogoutCommand;
import com.safestep.platform.iam.interfaces.rest.resources.LogoutResource;

public final class LogoutCommandFromResourceAssembler {
    private LogoutCommandFromResourceAssembler() {
    }

    public static LogoutCommand toCommandFromResource(LogoutResource resource) {
        return new LogoutCommand(resource.refreshToken());
    }
}
