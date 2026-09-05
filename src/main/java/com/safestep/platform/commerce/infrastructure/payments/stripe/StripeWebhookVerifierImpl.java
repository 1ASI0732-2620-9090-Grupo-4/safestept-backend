package com.safestep.platform.commerce.infrastructure.payments.stripe;

import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeWebhookVerifier;
import com.safestep.platform.commerce.domain.model.valueobjects.StripeWebhookEvent;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeWebhookVerifierImpl implements StripeWebhookVerifier {
    private final String webhookSecret;

    public StripeWebhookVerifierImpl(@Value("${stripe.webhook.secret:}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    @Override
    public Result<StripeWebhookEvent, ApplicationError> verify(String payload, String signature) {
        if (signature == null || signature.isBlank()) {
            return Result.failure(ApplicationError.validationError("stripeSignature",
                    "Stripe webhook signature is required"));
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return Result.failure(ApplicationError.unexpected("stripe webhook", "Stripe webhook secret is not configured"));
        }
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            var stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
            if (stripeObject instanceof Session session) {
                return Result.success(new StripeWebhookEvent(event.getType(), session.getId(),
                        session.getPaymentIntent()));
            }
            return Result.success(new StripeWebhookEvent(event.getType(), null, null));
        } catch (SignatureVerificationException exception) {
            return Result.failure(ApplicationError.validationError("stripeSignature", "Invalid Stripe webhook signature"));
        }
    }
}
