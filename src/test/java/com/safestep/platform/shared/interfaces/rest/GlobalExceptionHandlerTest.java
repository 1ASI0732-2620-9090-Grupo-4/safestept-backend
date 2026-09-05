package com.safestep.platform.shared.interfaces.rest;

import com.safestep.platform.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void handleRuntimeExceptionUsesLocalizedUnexpectedMessage() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        var handler = new GlobalExceptionHandler();
        var response = handler.handleRuntimeException(new RuntimeException("boom"));
        var error = Objects.requireNonNull((ErrorResource) response.getBody());

        assertEquals(500, response.getStatusCode().value());
        assertEquals("UNEXPECTED_ERROR", error.code());
        assertEquals("Error inesperado", error.message());
        assertEquals("boom", error.details());
    }

    @Test
    void handleIllegalArgumentExceptionReturnsValidationError() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var handler = new GlobalExceptionHandler();
        var response = handler
                .handleIllegalArgumentException(new IllegalArgumentException("Student record id must be a valid UUID"));
        var error = Objects.requireNonNull((ErrorResource) response.getBody());

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_ERROR", error.code());
        assertEquals("Validation failed", error.message());
        assertEquals("Student record id must be a valid UUID", error.details());
    }
}
