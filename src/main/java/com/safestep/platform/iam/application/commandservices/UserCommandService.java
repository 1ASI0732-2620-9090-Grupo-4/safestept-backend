package com.safestep.platform.iam.application.commandservices;

import com.safestep.platform.iam.domain.model.aggregates.User;
import com.safestep.platform.iam.domain.model.commands.ForgotPasswordCommand;
import com.safestep.platform.iam.domain.model.commands.LogoutCommand;
import com.safestep.platform.iam.domain.model.commands.RefreshTokenCommand;
import com.safestep.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.safestep.platform.iam.domain.model.commands.SignInCommand;
import com.safestep.platform.iam.domain.model.commands.SignUpCommand;
import com.safestep.platform.iam.domain.model.commands.UpdateUserStatusCommand;
import com.safestep.platform.iam.domain.model.valueobjects.AuthenticationTokens;
import com.safestep.platform.iam.domain.model.valueobjects.PasswordResetTokenIssue;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Application service contract for IAM user commands.
 */
public interface UserCommandService {
    /**
     * Handles user sign-in.
     *
     * @param command
     *            sign-in command
     *
     * @return authenticated user and token pair, or an application error
     */
    Result<ImmutablePair<User, AuthenticationTokens>, ApplicationError> handle(SignInCommand command);

    /**
     * Handles user sign-up.
     *
     * @param command
     *            sign-up command
     *
     * @return created user aggregate, or an application error
     */
    Result<User, ApplicationError> handle(SignUpCommand command);

    Result<ImmutablePair<User, AuthenticationTokens>, ApplicationError> handle(RefreshTokenCommand command);

    Result<String, ApplicationError> handle(LogoutCommand command);

    Result<PasswordResetTokenIssue, ApplicationError> handle(ForgotPasswordCommand command);

    Result<String, ApplicationError> handle(ResetPasswordCommand command);

    Result<User, ApplicationError> handle(UpdateUserStatusCommand command);

}
