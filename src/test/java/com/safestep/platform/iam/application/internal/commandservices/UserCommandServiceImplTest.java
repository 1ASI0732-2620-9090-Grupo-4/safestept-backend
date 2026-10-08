package com.safestep.platform.iam.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.safestep.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.safestep.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.safestep.platform.iam.domain.model.aggregates.User;
import com.safestep.platform.iam.domain.model.commands.SignInCommand;
import com.safestep.platform.iam.domain.model.commands.SignUpCommand;
import com.safestep.platform.iam.domain.model.commands.UpdateUserRolesCommand;
import com.safestep.platform.iam.domain.model.commands.UpdateUserStatusCommand;
import com.safestep.platform.iam.domain.model.entities.Role;
import com.safestep.platform.iam.domain.model.valueobjects.Roles;
import com.safestep.platform.iam.domain.repositories.PasswordResetTokenRepository;
import com.safestep.platform.iam.domain.repositories.RefreshTokenRepository;
import com.safestep.platform.iam.domain.repositories.RoleRepository;
import com.safestep.platform.iam.domain.repositories.UserRepository;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserCommandServiceImplTest {

    private UserRepository userRepository;
    private HashingService hashingService;
    private RoleRepository roleRepository;
    private UserCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        hashingService = mock(HashingService.class);
        roleRepository = mock(RoleRepository.class);
        service = new UserCommandServiceImpl(userRepository, hashingService, mock(TokenService.class), roleRepository,
                mock(RefreshTokenRepository.class), mock(PasswordResetTokenRepository.class), 30, 30);
    }

    private User user(long id, String username, Roles... roles) {
        var created = new User(username, "hash",
                java.util.Arrays.stream(roles).map(Role::new).toList());
        created.setId(id);
        return created;
    }

    private ApplicationError errorOf(Result<?, ApplicationError> result) {
        return ((Result.Failure<?, ApplicationError>) result).error();
    }

    @Test
    @DisplayName("handle(SignInCommand) should reject an unknown username (AAA)")
    void signIn_UnknownUser_ReturnsNotFound() {
        // Arrange
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new SignInCommand("ghost", "pwd"));

        // Assert
        assertTrue(result.isFailure());
        assertEquals("USER_NOT_FOUND", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(SignInCommand) should reject a wrong password (AAA)")
    void signIn_WrongPassword_ReturnsValidationError() {
        // Arrange
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user(1, "ana", Roles.ROLE_USER)));
        when(hashingService.matches("bad", "hash")).thenReturn(false);

        // Act
        var result = service.handle(new SignInCommand("ana", "bad"));

        // Assert
        assertEquals("VALIDATION_ERROR", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(SignInCommand) should reject disabled accounts (AAA)")
    void signIn_DisabledAccount_ReturnsBusinessRuleViolation() {
        // Arrange
        var disabled = user(1, "ana", Roles.ROLE_USER);
        disabled.setEnabled(false);
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(disabled));
        when(hashingService.matches("pwd", "hash")).thenReturn(true);

        // Act
        var result = service.handle(new SignInCommand("ana", "pwd"));

        // Assert
        assertEquals("BUSINESS_RULE_VIOLATION", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(SignUpCommand) should reject a duplicated username (AAA)")
    void signUp_DuplicatedUsername_ReturnsConflict() {
        // Arrange
        when(userRepository.existsByUsername("ana")).thenReturn(true);

        // Act
        var result = service.handle(new SignUpCommand("ana", "pwd", List.of("ROLE_USER")));

        // Assert
        assertEquals("USER_CONFLICT", errorOf(result).code());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(SignUpCommand) should fail when a requested role does not exist (AAA)")
    void signUp_UnknownRole_ReturnsNotFound() {
        // Arrange
        when(userRepository.existsByUsername("ana")).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_ADMIN)).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new SignUpCommand("ana", "pwd", List.of("ROLE_ADMIN")));

        // Assert
        assertEquals("ROLE_NOT_FOUND", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(UpdateUserStatusCommand) should update the account flags (AAA)")
    void updateStatus_ExistingUser_UpdatesFlags() {
        // Arrange
        var target = user(7, "ana", Roles.ROLE_USER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        // Act
        var result = service.handle(new UpdateUserStatusCommand(7L, false, true, true, true));

        // Assert
        assertTrue(result.isSuccess());
        assertFalse(target.isEnabled());
    }

    @Test
    @DisplayName("handle(UpdateUserStatusCommand) should fail for an unknown user (AAA)")
    void updateStatus_UnknownUser_ReturnsNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new UpdateUserStatusCommand(99L, true, true, true, true));

        // Assert
        assertEquals("USER_NOT_FOUND", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(UpdateUserRolesCommand) should promote a regular user to admin (AAA)")
    void updateRoles_PromotesUser() {
        // Arrange
        var target = user(7, "ana", Roles.ROLE_USER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(target));
        when(roleRepository.findByName(Roles.ROLE_ADMIN)).thenReturn(Optional.of(new Role(Roles.ROLE_ADMIN)));
        when(userRepository.save(target)).thenReturn(target);

        // Act
        var result = service.handle(new UpdateUserRolesCommand(7L, List.of("ROLE_ADMIN"), "boss"));

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(target.getRoles().stream().anyMatch(role -> role.getStringName().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("handle(UpdateUserRolesCommand) should fail for an unknown user (AAA)")
    void updateRoles_UnknownUser_ReturnsNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new UpdateUserRolesCommand(99L, List.of("ROLE_USER"), "boss"));

        // Assert
        assertEquals("USER_NOT_FOUND", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(UpdateUserRolesCommand) should fail when a role name has no stored role (AAA)")
    void updateRoles_UnknownRole_ReturnsNotFound() {
        // Arrange
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7, "ana", Roles.ROLE_USER)));
        when(roleRepository.findByName(Roles.ROLE_INSTRUCTOR)).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new UpdateUserRolesCommand(7L, List.of("ROLE_INSTRUCTOR"), "boss"));

        // Assert
        assertEquals("ROLE_NOT_FOUND", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(UpdateUserRolesCommand) should forbid an admin from removing their own admin role (AAA)")
    void updateRoles_SelfDemotion_IsForbidden() {
        // Arrange
        var admin = user(1, "boss", Roles.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(roleRepository.findByName(Roles.ROLE_USER)).thenReturn(Optional.of(new Role(Roles.ROLE_USER)));
        when(userRepository.findByUsername("boss")).thenReturn(Optional.of(admin));

        // Act
        var result = service.handle(new UpdateUserRolesCommand(1L, List.of("ROLE_USER"), "boss"));

        // Assert
        assertEquals("BUSINESS_RULE_VIOLATION", errorOf(result).code());
        assertTrue(errorOf(result).message().contains("self-role-removal"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(UpdateUserRolesCommand) should keep at least one admin in the system (AAA)")
    void updateRoles_LastAdminRemoval_IsForbidden() {
        // Arrange
        var lastAdmin = user(1, "boss", Roles.ROLE_ADMIN);
        var actingAdmin = user(2, "other", Roles.ROLE_USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(lastAdmin));
        when(roleRepository.findByName(Roles.ROLE_USER)).thenReturn(Optional.of(new Role(Roles.ROLE_USER)));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(actingAdmin));
        when(userRepository.findAll()).thenReturn(List.of(lastAdmin, actingAdmin));

        // Act
        var result = service.handle(new UpdateUserRolesCommand(1L, List.of("ROLE_USER"), "other"));

        // Assert
        assertEquals("BUSINESS_RULE_VIOLATION", errorOf(result).code());
        assertTrue(errorOf(result).message().contains("last-admin-removal"));
    }

    @Test
    @DisplayName("handle(UpdateUserRolesCommand) should demote an admin when another admin remains (AAA)")
    void updateRoles_DemotesAdminWhenAnotherRemains() {
        // Arrange
        var demoted = user(1, "ex-admin", Roles.ROLE_ADMIN);
        var remaining = user(2, "boss", Roles.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoted));
        when(roleRepository.findByName(Roles.ROLE_USER)).thenReturn(Optional.of(new Role(Roles.ROLE_USER)));
        when(userRepository.findByUsername("boss")).thenReturn(Optional.of(remaining));
        when(userRepository.findAll()).thenReturn(List.of(demoted, remaining));
        when(userRepository.save(demoted)).thenReturn(demoted);

        // Act
        var result = service.handle(new UpdateUserRolesCommand(1L, List.of("ROLE_USER"), "boss"));

        // Assert
        assertTrue(result.isSuccess());
        assertFalse(demoted.getRoles().stream().anyMatch(role -> role.getStringName().equals("ROLE_ADMIN")));
    }
}
