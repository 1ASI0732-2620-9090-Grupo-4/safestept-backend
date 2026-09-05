package com.safestep.platform.iam.interfaces.rest.transform;

import com.safestep.platform.iam.domain.model.commands.SignUpCommand;
import com.safestep.platform.iam.interfaces.rest.resources.SignUpResource;

import java.util.List;

public class SignUpCommandFromResourceAssembler {

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(resource.username(), resource.password(), List.of("ROLE_USER"));
    }
}
