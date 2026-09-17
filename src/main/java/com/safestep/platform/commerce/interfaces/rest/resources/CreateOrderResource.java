package com.safestep.platform.commerce.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record CreateOrderResource(
        @Pattern(regexp = "PENDING|PAID|SHIPPED|DELIVERED|CANCELLED|Comprado|Pendiente|Enviado|Entregado|Cancelado",
                message = "{validation.pattern}")
        @Schema(description = "Initial order status", example = "PENDING",
                allowableValues = {"PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED", "Comprado"}) String status,
        @Schema(description = "External id of a redeemed coupon to apply, if any") String redeemedCouponExternalId) {
}
