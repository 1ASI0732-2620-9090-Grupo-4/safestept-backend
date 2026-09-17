package com.safestep.platform.iam.domain.model.commands;

import java.util.List;

public record UpdateUserRolesCommand(Long userId, List<String> roleNames, String actingUsername) {
}
