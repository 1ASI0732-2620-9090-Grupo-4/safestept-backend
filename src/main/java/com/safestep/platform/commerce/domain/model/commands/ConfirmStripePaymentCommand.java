package com.safestep.platform.commerce.domain.model.commands;

public record ConfirmStripePaymentCommand(String username, String orderExternalId, String stripeSessionId) {
}
