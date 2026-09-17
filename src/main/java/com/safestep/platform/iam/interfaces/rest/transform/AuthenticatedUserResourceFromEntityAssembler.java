package com.safestep.platform.iam.interfaces.rest.transform;

import com.safestep.platform.iam.domain.model.aggregates.User;
import com.safestep.platform.iam.domain.model.valueobjects.AuthenticationTokens;
import com.safestep.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;

/**
 * Assembler that translates IAM authentication results into {@link AuthenticatedUserResource}.
 */
public class AuthenticatedUserResourceFromEntityAssembler {
    /**
     * Creates a resource from the authenticated {@link User} aggregate and issued bearer token.
     *
     * @param user
     *            authenticated user aggregate
     * @param tokens
     *            generated access and refresh tokens
     *
     * @return resource used by the authentication endpoint response
     */
    public static AuthenticatedUserResource toResourceFromEntity(User user, AuthenticationTokens tokens) {
        return new AuthenticatedUserResource(user.getId(), user.getUsername(), tokens.accessToken(),
                tokens.refreshToken(), user.getRoles().stream().map(role -> role.getStringName()).sorted().toList());
    }
}
