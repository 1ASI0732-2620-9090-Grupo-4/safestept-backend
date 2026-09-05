package com.safestep.platform.commerce.interfaces.rest.resources;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommerceResourcesValidationTest {
    private final jakarta.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void addCartItemRejectsBlankProductAndZeroQuantity() {
        var violations = validator.validate(new AddCartItemResource("", 0));

        assertFalse(violations.isEmpty());
    }

    @Test
    void createOrderRejectsUnknownStatus() {
        var violations = validator.validate(new CreateOrderResource("FINISHED_BY_CLIENT"));

        assertFalse(violations.isEmpty());
    }

    @Test
    void updateCartItemAcceptsPositiveQuantity() {
        var violations = validator.validate(new UpdateCartItemResource(2));

        assertTrue(violations.isEmpty());
    }
}
