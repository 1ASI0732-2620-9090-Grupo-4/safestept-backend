package com.safestep.platform.iam.interfaces.rest.transform;

import com.safestep.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.safestep.platform.iam.interfaces.rest.resources.ResetPasswordResource;

public final class ResetPasswordCommandFromResourceAssembler {
    private ResetPasswordCommandFromResourceAssembler() {
    }

    public static ResetPasswordCommand toCommandFromResource(ResetPasswordResource resource) {
        return new ResetPasswordCommand(resource.resetToken(), resource.newPassword());
    }
}
