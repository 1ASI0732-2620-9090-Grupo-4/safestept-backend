package com.safestep.platform.simulation.interfaces.rest.resources;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record CreateAttemptResource(
        @NotBlank(message = "{validation.not-blank}") String mode,
        @NotNull(message = "{validation.not-null}") Instant startedAt,
        Instant completedAt,
        @Min(0) @Max(100) int score,
        @Min(0) int totalSteps,
        @Min(0) int correctSteps,
        @Min(0) long timeElapsed,
        List<@Valid AttemptErrorResource> errors) {
}
