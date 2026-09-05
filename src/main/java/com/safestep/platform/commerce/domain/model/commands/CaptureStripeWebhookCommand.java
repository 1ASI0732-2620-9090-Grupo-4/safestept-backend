package com.safestep.platform.commerce.domain.model.commands;

public record CaptureStripeWebhookCommand(String payload, String signature) {
}
