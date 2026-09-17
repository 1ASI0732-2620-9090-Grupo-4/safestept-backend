package com.safestep.platform.iam.domain.model.commands;

/**
 * Seed admin command. Represents the command to bootstrap the first ROLE_ADMIN user from environment configuration,
 * only if no admin exists yet.
 */
public record SeedAdminCommand() {
}
