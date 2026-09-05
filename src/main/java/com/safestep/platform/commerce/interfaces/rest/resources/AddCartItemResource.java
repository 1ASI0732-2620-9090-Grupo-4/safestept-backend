package com.safestep.platform.commerce.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddCartItemResource(
        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Product public identifier", example = "kit-hogar") String productId,

        @Min(value = 1, message = "{validation.min}")
        @Schema(description = "Quantity to add", example = "1", minimum = "1") int quantity) {
}
