package com.safestep.platform.iam.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.safestep.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.safestep.platform.iam.domain.model.aggregates.User;
import com.safestep.platform.iam.domain.model.commands.SeedAdminCommand;
import com.safestep.platform.iam.domain.model.entities.Role;
import com.safestep.platform.iam.domain.model.valueobjects.Roles;
import com.safestep.platform.iam.domain.repositories.RoleRepository;
import com.safestep.platform.iam.domain.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AdminSeedCommandServiceImplTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private HashingService hashingService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        hashingService = mock(HashingService.class);
    }

    private AdminSeedCommandServiceImpl service(String username, String password) {
        return new AdminSeedCommandServiceImpl(userRepository, roleRepository, hashingService, username, password);
    }

    @Test
    @DisplayName("handle should skip the seed when the credentials are not configured (AAA)")
    void handle_WithoutCredentials_DoesNothing() {
        // Act
        service("", "").handle(new SeedAdminCommand());
        service(null, null).handle(new SeedAdminCommand());

        // Assert
        verifyNoInteractions(userRepository, roleRepository, hashingService);
    }

    @Test
    @DisplayName("handle should skip the seed when an admin already exists (AAA)")
    void handle_WhenAdminExists_DoesNothing() {
        // Arrange
        var existingAdmin = new User("boss", "hash", List.of(new Role(Roles.ROLE_ADMIN)));
        when(userRepository.findAll()).thenReturn(List.of(existingAdmin));

        // Act
        service("admin", "secret").handle(new SeedAdminCommand());

        // Assert
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle should skip the seed when the username belongs to a non-admin user (AAA)")
    void handle_WhenUsernameTaken_DoesNothing() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(new User("ana", "hash", List.of(Role.getDefaultRole()))));
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        // Act
        service("admin", "secret").handle(new SeedAdminCommand());

        // Assert
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle should skip the seed when ROLE_ADMIN has not been seeded yet (AAA)")
    void handle_WhenAdminRoleMissing_DoesNothing() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_ADMIN)).thenReturn(Optional.empty());

        // Act
        service("admin", "secret").handle(new SeedAdminCommand());

        // Assert
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle should create the bootstrap admin with an encoded password (AAA)")
    void handle_WhenEverythingIsReady_CreatesAdmin() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_ADMIN)).thenReturn(Optional.of(new Role(Roles.ROLE_ADMIN)));
        when(hashingService.encode("secret")).thenReturn("encoded-secret");

        // Act
        service("admin", "secret").handle(new SeedAdminCommand());

        // Assert
        var saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals("admin", saved.getValue().getUsername());
        assertEquals("encoded-secret", saved.getValue().getPassword());
        assertTrue(saved.getValue().getRoles().stream().anyMatch(role -> role.getStringName().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("handle should never let a persistence failure stop the application (AAA)")
    void handle_WhenSaveFails_SwallowsTheException() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_ADMIN)).thenReturn(Optional.of(new Role(Roles.ROLE_ADMIN)));
        when(hashingService.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenThrow(new IllegalStateException("db down"));

        // Act + Assert
        service("admin", "secret").handle(new SeedAdminCommand());
        verify(userRepository).save(any(User.class));
    }
}
