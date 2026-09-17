package com.safestep.platform.iam.application.commandservices;

import com.safestep.platform.iam.domain.model.commands.SeedAdminCommand;

/**
 * Application service contract for bootstrapping the initial admin user.
 */
public interface AdminSeedCommandService {
    /**
     * Handles the admin seeding command. Creates a ROLE_ADMIN user from environment configuration, but only when no
     * admin exists yet and the required environment variables are set. Must never prevent the application from
     * starting.
     *
     * @param command
     *            admin-seeding command
     */
    void handle(SeedAdminCommand command);
}
