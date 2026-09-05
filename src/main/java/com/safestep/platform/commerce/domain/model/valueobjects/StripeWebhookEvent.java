package com.safestep.platform.commerce.domain.model.valueobjects;

public record StripeWebhookEvent(String type, String sessionId, String paymentIntentId) {
    public boolean isCheckoutSessionCompleted() {
        return "checkout.session.completed".equals(type);
    }

    public boolean isCheckoutSessionExpired() {
        return "checkout.session.expired".equals(type);
    }
}
