package com.safestep.platform.commerce.infrastructure.payments.stripe;

import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeCheckoutClient;
import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.valueobjects.StripeCheckoutSession;
import com.safestep.platform.commerce.domain.model.valueobjects.StripePaymentConfirmation;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StripeCheckoutClientImpl implements StripeCheckoutClient {
    private final String currency;
    private final String successUrl;
    private final String cancelUrl;
    private final String stripeSecretKey;

    public StripeCheckoutClientImpl(@Value("${stripe.currency:usd}") String currency,
            @Value("${stripe.success-url:http://localhost:4200/payment/success}") String successUrl,
            @Value("${stripe.cancel-url:http://localhost:4200/payment/cancel}") String cancelUrl,
            @Value("${stripe.secret.key:}") String stripeSecretKey) {
        this.currency = currency;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
        this.stripeSecretKey = stripeSecretKey;
    }

    @Override
    public Result<StripeCheckoutSession, ApplicationError> createCheckoutSession(Order order) {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            return Result.failure(ApplicationError.unexpected("stripe", "Stripe secret key is not configured"));
        }
        try {
            var builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(withOrderQuery(successUrl, order.getExternalId()))
                    .setCancelUrl(withOrderQuery(cancelUrl, order.getExternalId()))
                    .putMetadata("orderId", order.getExternalId())
                    .putMetadata("username", order.getUsername());

            order.getItems().forEach(item -> builder.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.quantity())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(currency)
                            .setUnitAmount(toMinorUnits(item.unitPrice()))
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.productName())
                                    .setDescription("SafeStep order " + order.getExternalId())
                                    .build())
                            .build())
                    .build()));

            var session = Session.create(builder.build());
            return Result.success(new StripeCheckoutSession(session.getId(), session.getUrl()));
        } catch (StripeException exception) {
            return Result.failure(ApplicationError.unexpected("stripe checkout", exception.getMessage()));
        }
    }

    @Override
    public Result<StripePaymentConfirmation, ApplicationError> retrieveCheckoutSession(String sessionId) {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            return Result.failure(ApplicationError.unexpected("stripe", "Stripe secret key is not configured"));
        }
        if (sessionId == null || sessionId.isBlank()) {
            return Result.failure(ApplicationError.validationError("stripe session", "Stripe session id is required"));
        }
        try {
            var session = Session.retrieve(sessionId);
            return Result.success(new StripePaymentConfirmation(session.getId(), session.getPaymentStatus(),
                    session.getPaymentIntent()));
        } catch (StripeException exception) {
            return Result.failure(ApplicationError.unexpected("stripe session", exception.getMessage()));
        }
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private String withOrderQuery(String url, String orderId) {
        var separator = url.contains("?") ? "&" : "?";
        return url + separator + "orderId=" + orderId + "&session_id={CHECKOUT_SESSION_ID}";
    }
}
