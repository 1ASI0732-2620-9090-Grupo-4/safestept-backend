package com.safestep.platform.commerce.domain.model.commands;

public record CancelStripePaymentCommand(String username, String orderExternalId, String stripeSessionId) {
}
