package com.safestep.platform.commerce.application.internal.outboundservices.stripe;

import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.valueobjects.StripeCheckoutSession;
import com.safestep.platform.commerce.domain.model.valueobjects.StripePaymentConfirmation;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;

public interface StripeCheckoutClient {
    Result<StripeCheckoutSession, ApplicationError> createCheckoutSession(Order order);

    Result<StripePaymentConfirmation, ApplicationError> retrieveCheckoutSession(String sessionId);
}
