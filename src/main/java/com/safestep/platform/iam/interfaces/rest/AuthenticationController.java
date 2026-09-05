package com.safestep.platform.iam.interfaces.rest;

import com.safestep.platform.iam.application.commandservices.UserCommandService;
import com.safestep.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.safestep.platform.iam.interfaces.rest.resources.ForgotPasswordResource;
import com.safestep.platform.iam.interfaces.rest.resources.ForgotPasswordResponseResource;
import com.safestep.platform.iam.interfaces.rest.resources.LogoutResource;
import com.safestep.platform.iam.interfaces.rest.resources.MessageResource;
import com.safestep.platform.iam.interfaces.rest.resources.RefreshTokenResource;
import com.safestep.platform.iam.interfaces.rest.resources.ResetPasswordResource;
import com.safestep.platform.iam.interfaces.rest.resources.SignInResource;
import com.safestep.platform.iam.interfaces.rest.resources.SignUpResource;
import com.safestep.platform.iam.interfaces.rest.resources.UserResource;
import com.safestep.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.safestep.platform.iam.interfaces.rest.transform.ForgotPasswordCommandFromResourceAssembler;
import com.safestep.platform.iam.interfaces.rest.transform.LogoutCommandFromResourceAssembler;
import com.safestep.platform.iam.interfaces.rest.transform.RefreshTokenCommandFromResourceAssembler;
import com.safestep.platform.iam.interfaces.rest.transform.ResetPasswordCommandFromResourceAssembler;
import com.safestep.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.safestep.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.safestep.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.safestep.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthenticationController
 * <p>
 * This controller is responsible for handling authentication requests. It exposes authentication endpoints:
 * <ul>
 * <li>POST /api/v1/authentication/sign-in</li>
 * <li>POST /api/v1/authentication/sign-up</li>
 * <li>POST /api/v1/authentication/refresh-token</li>
 * <li>POST /api/v1/authentication/logout</li>
 * <li>POST /api/v1/authentication/forgot-password</li>
 * <li>POST /api/v1/authentication/reset-password</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Handles the sign-in request.
     *
     * @param signInResource
     *            the sign-in request body with username and password.
     *
     * @return the authenticated user resource with JWT token.
     */
    @PostMapping("/sign-in")
    @Operation(summary = "User sign-in", description = "Authenticates a user and returns access and refresh tokens for subsequent requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticatedUserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or malformed request", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found with provided username", content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var result = userCommandService.handle(signInCommand);
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                auth -> AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(auth.getLeft(),
                        auth.getRight()),
                HttpStatus.OK);
    }

    /**
     * Handles the sign-up request.
     *
     * @param signUpResource
     *            the sign-up request body with username and password.
     *
     * @return the created user resource with assigned roles.
     */
    @PostMapping("/sign-up")
    @Operation(summary = "User registration", description = "Creates a new public user account. Public registration always assigns ROLE_USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or username already exists", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Conflict - username already taken", content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var result = userCommandService.handle(signUpCommand);
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                UserResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);

    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh authentication tokens", description = "Validates a refresh token, revokes it, and returns a new access/refresh token pair.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens rotated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticatedUserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token or malformed request", content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenResource refreshTokenResource) {
        var command = RefreshTokenCommandFromResourceAssembler.toCommandFromResource(refreshTokenResource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                auth -> AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(auth.getLeft(),
                        auth.getRight()),
                HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the provided refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refresh token revoked", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token or malformed request", content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutResource logoutResource) {
        var command = LogoutCommandFromResourceAssembler.toCommandFromResource(logoutResource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result, MessageResource::new, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset token", description = "Generates a temporary password reset token. Local/dev mode returns the token in the response because SMTP is not configured.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset token generated in local/dev mode", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ForgotPasswordResponseResource.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordResource forgotPasswordResource) {
        var command = ForgotPasswordCommandFromResourceAssembler.toCommandFromResource(forgotPasswordResource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                issue -> new ForgotPasswordResponseResource(issue.username(), issue.resetToken(), issue.expiresAt(),
                        "LOCAL_DEV_RESPONSE"),
                HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Updates the user password with a valid temporary token and revokes active refresh tokens for that user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset completed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid reset token or malformed request", content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordResource resetPasswordResource) {
        var command = ResetPasswordCommandFromResourceAssembler.toCommandFromResource(resetPasswordResource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result, MessageResource::new, HttpStatus.OK);
    }
}
