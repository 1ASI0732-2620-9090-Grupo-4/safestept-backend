package com.safestep.platform.iam.domain.model.commands;

public record UpdateUserStatusCommand(Long userId, boolean enabled, boolean accountNonLocked,
        boolean accountNonExpired, boolean credentialsNonExpired) {
}
