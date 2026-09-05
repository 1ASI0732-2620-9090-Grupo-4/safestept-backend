package com.safestep.platform.iam.interfaces.rest.transform;

import com.safestep.platform.iam.domain.model.commands.ForgotPasswordCommand;
import com.safestep.platform.iam.interfaces.rest.resources.ForgotPasswordResource;

public final class ForgotPasswordCommandFromResourceAssembler {
    private ForgotPasswordCommandFromResourceAssembler() {
    }

    public static ForgotPasswordCommand toCommandFromResource(ForgotPasswordResource resource) {
        return new ForgotPasswordCommand(resource.username());
    }
}
