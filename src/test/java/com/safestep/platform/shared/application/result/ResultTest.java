package com.safestep.platform.shared.application.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultTest {

    private final Result<Integer, String> success = Result.success(21);
    private final Result<Integer, String> failure = Result.failure("boom");

    @Test
    @DisplayName("success and failure factories should report their state (AAA)")
    void factories_ReportState() {
        // Assert
        assertTrue(success.isSuccess());
        assertFalse(success.isFailure());
        assertTrue(failure.isFailure());
        assertFalse(failure.isSuccess());
    }

    @Test
    @DisplayName("toOptional should only contain the value of a success (AAA)")
    void toOptional_OnlyForSuccess() {
        // Assert
        assertEquals(21, success.toOptional().orElseThrow());
        assertTrue(failure.toOptional().isEmpty());
    }

    @Test
    @DisplayName("getOrElse should fall back to the default for failures (AAA)")
    void getOrElse_FallsBackForFailure() {
        // Assert
        assertEquals(21, success.getOrElse(0));
        assertEquals(0, failure.getOrElse(0));
    }

    @Test
    @DisplayName("map should transform successes and keep failures untouched (AAA)")
    void map_TransformsOnlySuccess() {
        // Act
        var mappedSuccess = success.map(value -> value * 2);
        var mappedFailure = failure.map(value -> value * 2);

        // Assert
        assertEquals(42, mappedSuccess.getOrElse(0));
        assertTrue(mappedFailure.isFailure());
    }

    @Test
    @DisplayName("flatMap should chain successes and short-circuit failures (AAA)")
    void flatMap_ChainsOnlySuccess() {
        // Act
        Result<String, String> chained = success.flatMap(value -> Result.success("v" + value));
        Result<String, String> rejected = success.flatMap(value -> Result.failure("rejected"));
        Result<String, String> shortCircuited = failure.flatMap(value -> Result.success("never"));

        // Assert
        assertEquals("v21", chained.getOrElse(""));
        assertTrue(rejected.isFailure());
        assertTrue(shortCircuited.isFailure());
    }

    @Test
    @DisplayName("mapError should translate the error of failures only (AAA)")
    void mapError_TranslatesOnlyFailure() {
        // Act
        var mappedFailure = failure.mapError(String::length);
        var mappedSuccess = success.mapError(String::length);

        // Assert
        assertTrue(mappedFailure instanceof Result.Failure<Integer, Integer> f && f.error() == 4);
        assertEquals(21, mappedSuccess.getOrElse(0));
    }

    @Test
    @DisplayName("recover should replace a failure and leave a success alone (AAA)")
    void recover_ReplacesOnlyFailure() {
        // Act
        var recovered = failure.recover(error -> Result.success(-1));
        var untouched = success.recover(error -> Result.success(-1));

        // Assert
        assertEquals(-1, recovered.getOrElse(0));
        assertEquals(21, untouched.getOrElse(0));
    }
}
