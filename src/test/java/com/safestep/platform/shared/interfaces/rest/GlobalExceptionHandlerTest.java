package com.safestep.platform.shared.interfaces.rest;

import com.safestep.platform.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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
    void handleHttpMessageNotReadableAnswers400WithoutLeakingTheParserMessage() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        var exception = new HttpMessageNotReadableException("JSON parse error: Unexpected character ('x')",
                mock(HttpInputMessage.class));

        var response = new GlobalExceptionHandler().handleHttpMessageNotReadable(exception);
        var error = Objects.requireNonNull((ErrorResource) response.getBody());

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_ERROR", error.code());
        assertEquals("Cuerpo de la solicitud mal formado o ilegible", error.details());
        assertFalse(error.details().contains("Unexpected character"));
    }

    @SuppressWarnings("unused")
    private void dummyHandlerMethod(String payload) {
    }

    @Test
    void handleMethodArgumentNotValidJoinsEveryFieldError() throws NoSuchMethodException {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        var binding = new BeanPropertyBindingResult(new Object(), "payload");
        binding.addError(new FieldError("payload", "name", "must not be blank"));
        binding.addError(new FieldError("payload", "email", "must be a valid email"));
        var parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyHandlerMethod", String.class), 0);

        var response = new GlobalExceptionHandler()
                .handleMethodArgumentNotValid(new MethodArgumentNotValidException(parameter, binding));
        var error = Objects.requireNonNull((ErrorResource) response.getBody());

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_ERROR", error.code());
        assertTrue(error.details().contains("name: must not be blank"));
        assertTrue(error.details().contains("email: must be a valid email"));
    }

    @Test
    void handleMethodArgumentNotValidWithoutFieldErrorsUsesGenericMessage() throws NoSuchMethodException {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        var binding = new BeanPropertyBindingResult(new Object(), "payload");
        var parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyHandlerMethod", String.class), 0);

        var response = new GlobalExceptionHandler()
                .handleMethodArgumentNotValid(new MethodArgumentNotValidException(parameter, binding));
        var error = Objects.requireNonNull((ErrorResource) response.getBody());

        assertEquals("Request validation failed", error.details());
    }

    @Test
    void handleAccessDeniedExceptionReturnsForbidden() {
        var response = new GlobalExceptionHandler().handleAccessDeniedException(new AccessDeniedException("nope"));
        var error = Objects.requireNonNull((ErrorResource) response.getBody());

        assertEquals(403, response.getStatusCode().value());
        assertEquals("FORBIDDEN", error.code());
        assertEquals("nope", error.details());
    }

    @Test
    void handleExceptionFallsBackToInternalServerError() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var response = new GlobalExceptionHandler().handleException(new Exception());
        var error = Objects.requireNonNull((ErrorResource) response.getBody());

        assertEquals(500, response.getStatusCode().value());
        assertEquals("An unexpected error occurred", error.details());
    }

    @Test
    void handleIllegalArgumentExceptionWithoutMessageUsesDefaultDetails() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var response = new GlobalExceptionHandler().handleIllegalArgumentException(new IllegalArgumentException());
        var error = Objects.requireNonNull((ErrorResource) response.getBody());

        assertEquals("Request validation failed", error.details());
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
