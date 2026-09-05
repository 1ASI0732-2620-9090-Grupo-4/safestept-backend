package com.safestep.platform.simulation.interfaces.rest.resources;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateAttemptResourceValidationTest {
    private final jakarta.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidAttemptPayload() {
        var resource = new CreateAttemptResource("", null, null, 120, 0, 0, -1,
                List.of(new AttemptErrorResource(0, "", "")));

        var violations = validator.validate(resource);

        assertFalse(violations.isEmpty());
    }

    @Test
    void acceptsValidAttemptPayload() {
        var resource = new CreateAttemptResource("practice", Instant.now(), Instant.now(), 90, 4, 4, 120,
                List.of());

        var violations = validator.validate(resource);

        assertTrue(violations.isEmpty());
    }
}
