package com.safestep.platform.iam.interfaces.rest.resources;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationResourcesValidationTest {
    private final jakarta.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void signInRejectsBlankUsernameAndShortPassword() {
        var violations = validator.validate(new SignInResource("", "123"));

        assertFalse(violations.isEmpty());
    }

    @Test
    void signUpAcceptsValidCredentials() {
        var violations = validator.validate(new SignUpResource("student.safe", "SecurePass123!", List.of("ROLE_USER")));

        assertTrue(violations.isEmpty());
    }
}
