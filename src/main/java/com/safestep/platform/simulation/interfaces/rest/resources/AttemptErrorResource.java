package com.safestep.platform.simulation.interfaces.rest.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AttemptErrorResource(
        @Min(1) int stepNumber,
        @NotBlank(message = "{validation.not-blank}") String error,
        @NotBlank(message = "{validation.not-blank}") String severity) {
}
