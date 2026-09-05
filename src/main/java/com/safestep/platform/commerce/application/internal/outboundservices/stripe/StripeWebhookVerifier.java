package com.safestep.platform.commerce.application.internal.outboundservices.stripe;

import com.safestep.platform.commerce.domain.model.valueobjects.StripeWebhookEvent;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;

public interface StripeWebhookVerifier {
    Result<StripeWebhookEvent, ApplicationError> verify(String payload, String signature);
}
