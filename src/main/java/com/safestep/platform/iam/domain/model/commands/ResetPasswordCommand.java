package com.safestep.platform.iam.domain.model.commands;

public record ResetPasswordCommand(String resetToken, String newPassword) {
}
