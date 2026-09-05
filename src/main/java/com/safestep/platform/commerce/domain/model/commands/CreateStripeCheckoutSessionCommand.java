package com.safestep.platform.commerce.domain.model.commands;

public record CreateStripeCheckoutSessionCommand(String username, String orderExternalId) {
}
