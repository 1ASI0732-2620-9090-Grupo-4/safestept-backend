package com.safestep.platform.commerce.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record UpdateCartItemResource(
        @Min(value = 1, message = "{validation.min}")
        @Schema(description = "Updated item quantity", example = "2", minimum = "1") int quantity) {
}
