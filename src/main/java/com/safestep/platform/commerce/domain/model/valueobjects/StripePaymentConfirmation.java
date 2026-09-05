package com.safestep.platform.commerce.domain.model.valueobjects;

public record StripePaymentConfirmation(String sessionId, String paymentStatus, String paymentIntentId) {
    public boolean isPaid() {
        return paymentStatus != null && paymentStatus.equalsIgnoreCase("paid");
    }
}
