package com.safestep.platform.iam.application.internal.commandservices;

import com.safestep.platform.iam.application.commandservices.AdminSeedCommandService;
import com.safestep.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.safestep.platform.iam.domain.model.aggregates.User;
import com.safestep.platform.iam.domain.model.commands.SeedAdminCommand;
import com.safestep.platform.iam.domain.model.valueobjects.Roles;
import com.safestep.platform.iam.domain.repositories.RoleRepository;
import com.safestep.platform.iam.domain.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of {@link AdminSeedCommandService}. Bootstraps a single ROLE_ADMIN user from environment
 * configuration on first startup, so the role-management screens have someone able to sign in and use them. It is a
 * no-op whenever the environment variables are unset or an admin already exists, and it never lets a bootstrap
 * problem stop the application from starting.
 */
@Service
@Slf4j
public class AdminSeedCommandServiceImpl implements AdminSeedCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final String adminUsername;
    private final String adminPassword;

    public AdminSeedCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
            HashingService hashingService, @Value("${safestep.admin-seed.username:}") String adminUsername,
            @Value("${safestep.admin-seed.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void handle(SeedAdminCommand command) {
        if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("Admin seed skipped: SAFESTEP_ADMIN_USERNAME/SAFESTEP_ADMIN_PASSWORD not set");
            return;
        }

        var anyAdminExists = userRepository.findAll().stream().anyMatch(
                user -> user.getRoles().stream().anyMatch(role -> role.getStringName().equals("ROLE_ADMIN")));
        if (anyAdminExists) {
            log.info("Admin seed skipped: an admin user already exists");
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            log.warn("Admin seed skipped: username '{}' is already taken by a non-admin user", adminUsername);
            return;
        }

        var adminRole = roleRepository.findByName(Roles.ROLE_ADMIN);
        if (adminRole.isEmpty()) {
            log.warn("Admin seed skipped: ROLE_ADMIN is not seeded yet");
            return;
        }

        try {
            var admin = new User(adminUsername, hashingService.encode(adminPassword), List.of(adminRole.get()));
            userRepository.save(admin);
            log.info("Bootstrap admin user '{}' created", adminUsername);
        } catch (RuntimeException exception) {
            log.warn("Admin seed failed, application will continue starting", exception);
        }
    }
}
