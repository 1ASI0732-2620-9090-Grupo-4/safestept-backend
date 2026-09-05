package com.safestep.platform.iam.application.internal.commandservices;

import com.safestep.platform.iam.application.commandservices.UserCommandService;
import com.safestep.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.safestep.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.safestep.platform.iam.domain.model.aggregates.PasswordResetToken;
import com.safestep.platform.iam.domain.model.aggregates.RefreshToken;
import com.safestep.platform.iam.domain.model.aggregates.User;
import com.safestep.platform.iam.domain.model.commands.ForgotPasswordCommand;
import com.safestep.platform.iam.domain.model.commands.LogoutCommand;
import com.safestep.platform.iam.domain.model.commands.RefreshTokenCommand;
import com.safestep.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.safestep.platform.iam.domain.model.commands.SignInCommand;
import com.safestep.platform.iam.domain.model.commands.SignUpCommand;
import com.safestep.platform.iam.domain.model.commands.UpdateUserStatusCommand;
import com.safestep.platform.iam.domain.model.entities.Role;
import com.safestep.platform.iam.domain.model.valueobjects.AuthenticationTokens;
import com.safestep.platform.iam.domain.model.valueobjects.PasswordResetTokenIssue;
import com.safestep.platform.iam.domain.repositories.PasswordResetTokenRepository;
import com.safestep.platform.iam.domain.repositories.RefreshTokenRepository;
import com.safestep.platform.iam.domain.repositories.RoleRepository;
import com.safestep.platform.iam.domain.repositories.UserRepository;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/**
 * User command service implementation.
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final int refreshTokenExpirationDays;
    private final int passwordResetExpirationMinutes;

    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService,
            TokenService tokenService, RoleRepository roleRepository, RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            @Value("${authorization.jwt.refresh.expiration.days:30}") int refreshTokenExpirationDays,
            @Value("${authorization.password-reset.expiration.minutes:30}") int passwordResetExpirationMinutes) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.passwordResetExpirationMinutes = passwordResetExpirationMinutes;
    }

    @Override
    public Result<ImmutablePair<User, AuthenticationTokens>, ApplicationError> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.username()));
        }
        if (!hashingService.matches(command.password(), user.get().getPassword())) {
            return Result.failure(ApplicationError.validationError("credentials", "Invalid username or password"));
        }
        if (!isAccountUsable(user.get())) {
            return Result.failure(ApplicationError.businessRuleViolation("user-account-status",
                    "User account is disabled, locked, expired, or credentials are expired"));
        }
        return Result.success(ImmutablePair.of(user.get(), issueTokensFor(user.get().getUsername())));
    }

    @Override
    public Result<User, ApplicationError> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            return Result.failure(ApplicationError.conflict("User", "Username already exists"));
        }
        var roles = command.roles().stream()
                .map(name -> roleRepository.findByName(Role.toRoleFromName(name).getName()))
                .toList();

        if (roles.stream().anyMatch(java.util.Optional::isEmpty)) {
            return Result.failure(ApplicationError.notFound("Role", "one or more role names"));
        }

        var resolvedRoles = roles.stream().map(java.util.Optional::get).toList();

        var user = new User(command.username(), hashingService.encode(command.password()), resolvedRoles);
        userRepository.save(user);
        return userRepository.findByUsername(command.username()).<Result<User, ApplicationError>> map(Result::success)
                .orElseGet(() -> Result
                        .failure(ApplicationError.unexpected("sign-up", "Created user could not be reloaded")));
    }

    @Override
    public Result<ImmutablePair<User, AuthenticationTokens>, ApplicationError> handle(RefreshTokenCommand command) {
        var now = Instant.now();
        var existingToken = refreshTokenRepository.findByTokenHash(hashToken(command.refreshToken()));
        if (existingToken.isEmpty() || !existingToken.get().isActive(now)) {
            return Result.failure(ApplicationError.validationError("refreshToken", "Refresh token is invalid"));
        }
        var user = userRepository.findByUsername(existingToken.get().getUsername());
        if (user.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", existingToken.get().getUsername()));
        }
        if (!isAccountUsable(user.get())) {
            return Result.failure(ApplicationError.businessRuleViolation("user-account-status",
                    "User account is disabled, locked, expired, or credentials are expired"));
        }
        existingToken.get().revoke(now);
        refreshTokenRepository.save(existingToken.get());
        return Result.success(ImmutablePair.of(user.get(), issueTokensFor(user.get().getUsername())));
    }

    @Override
    public Result<String, ApplicationError> handle(LogoutCommand command) {
        var token = refreshTokenRepository.findByTokenHash(hashToken(command.refreshToken()));
        if (token.isEmpty()) {
            return Result.failure(ApplicationError.validationError("refreshToken", "Refresh token is invalid"));
        }
        if (!token.get().isRevoked()) {
            token.get().revoke(Instant.now());
            refreshTokenRepository.save(token.get());
        }
        return Result.success("Logout completed");
    }

    @Override
    public Result<PasswordResetTokenIssue, ApplicationError> handle(ForgotPasswordCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.username()));
        }
        var plainToken = generateOpaqueToken();
        var expiresAt = Instant.now().plus(passwordResetExpirationMinutes, ChronoUnit.MINUTES);
        passwordResetTokenRepository
                .save(new PasswordResetToken(null, hashToken(plainToken), user.get().getUsername(), expiresAt, null));
        return Result.success(new PasswordResetTokenIssue(user.get().getUsername(), plainToken, expiresAt));
    }

    @Override
    public Result<String, ApplicationError> handle(ResetPasswordCommand command) {
        var now = Instant.now();
        var resetToken = passwordResetTokenRepository.findByTokenHash(hashToken(command.resetToken()));
        if (resetToken.isEmpty() || !resetToken.get().isActive(now)) {
            return Result.failure(ApplicationError.validationError("resetToken", "Reset token is invalid"));
        }
        var user = userRepository.findByUsername(resetToken.get().getUsername());
        if (user.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", resetToken.get().getUsername()));
        }
        user.get().setPassword(hashingService.encode(command.newPassword()));
        userRepository.save(user.get());
        resetToken.get().markUsed(now);
        passwordResetTokenRepository.save(resetToken.get());
        revokeActiveRefreshTokens(user.get().getUsername(), now);
        return Result.success("Password reset completed");
    }

    @Override
    public Result<User, ApplicationError> handle(UpdateUserStatusCommand command) {
        var user = userRepository.findById(command.userId());
        if (user.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.userId().toString()));
        }
        user.get().updateStatus(command.enabled(), command.accountNonLocked(), command.accountNonExpired(),
                command.credentialsNonExpired());
        return Result.success(userRepository.save(user.get()));
    }

    private AuthenticationTokens issueTokensFor(String username) {
        var accessToken = tokenService.generateToken(username);
        var refreshToken = generateOpaqueToken();
        refreshTokenRepository.save(new RefreshToken(null, hashToken(refreshToken), username,
                Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS), null));
        return new AuthenticationTokens(accessToken, refreshToken);
    }

    private void revokeActiveRefreshTokens(String username, Instant revokedAt) {
        refreshTokenRepository.findActiveByUsername(username).forEach(refreshToken -> {
            refreshToken.revoke(revokedAt);
            refreshTokenRepository.save(refreshToken);
        });
    }

    private boolean isAccountUsable(User user) {
        return user.isEnabled() && user.isAccountNonLocked() && user.isAccountNonExpired()
                && user.isCredentialsNonExpired();
    }

    private static String generateOpaqueToken() {
        var bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
