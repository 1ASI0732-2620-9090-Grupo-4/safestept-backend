package com.safestep.platform.commerce.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.safestep.platform.commerce.domain.model.entities.OrderItem;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.PaymentStatus;
import java.math.*;
import java.time.*;
import java.util.*;

public class Order extends AbstractDomainAggregateRoot<Order> {
    private Long id;
    private final String externalId, username;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final LocalDate createdAt;
    private String paymentProvider;
    private PaymentStatus paymentStatus;
    private String stripeCheckoutSessionId;
    private String stripePaymentIntentId;
    private Instant paidAt;
    private Integer appliedDiscountPercentage;
    private String redeemedCouponExternalId;

    public Order(Long id, String externalId, String username, List<OrderItem> items, OrderStatus status, LocalDate at) {
        this(id, externalId, username, items, status, at, null, PaymentStatus.NONE, null, null, null, null, null);
    }

    public Order(Long id, String externalId, String username, List<OrderItem> items, OrderStatus status, LocalDate at,
            String paymentProvider, PaymentStatus paymentStatus, String stripeCheckoutSessionId,
            String stripePaymentIntentId, Instant paidAt) {
        this(id, externalId, username, items, status, at, paymentProvider, paymentStatus, stripeCheckoutSessionId,
                stripePaymentIntentId, paidAt, null, null);
    }

    public Order(Long id, String externalId, String username, List<OrderItem> items, OrderStatus status, LocalDate at,
            String paymentProvider, PaymentStatus paymentStatus, String stripeCheckoutSessionId,
            String stripePaymentIntentId, Instant paidAt, Integer appliedDiscountPercentage,
            String redeemedCouponExternalId) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Order must contain items");
        this.id = id;
        this.externalId = externalId;
        this.username = username;
        this.items = List.copyOf(items);
        this.status = status == null ? OrderStatus.PENDING : status;
        createdAt = at;
        this.paymentProvider = paymentProvider;
        this.paymentStatus = paymentStatus == null ? PaymentStatus.NONE : paymentStatus;
        this.stripeCheckoutSessionId = stripeCheckoutSessionId;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.paidAt = paidAt;
        this.appliedDiscountPercentage = appliedDiscountPercentage;
        this.redeemedCouponExternalId = redeemedCouponExternalId;
    }

    public BigDecimal total() {
        return items.stream().map(OrderItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal finalTotal() {
        var total = total();
        if (appliedDiscountPercentage == null || appliedDiscountPercentage <= 0)
            return total;
        var discount = total.multiply(BigDecimal.valueOf(appliedDiscountPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return total.subtract(discount);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        id = v;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getUsername() {
        return username;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getStripeCheckoutSessionId() {
        return stripeCheckoutSessionId;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Integer getAppliedDiscountPercentage() {
        return appliedDiscountPercentage;
    }

    public String getRedeemedCouponExternalId() {
        return redeemedCouponExternalId;
    }

    public boolean canStartStripeCheckout() {
        return status != OrderStatus.PAID && status != OrderStatus.CANCELLED && paymentStatus != PaymentStatus.PAID
                && paymentStatus != PaymentStatus.PENDING;
    }

    public void startStripeCheckout(String sessionId) {
        if (sessionId == null || sessionId.isBlank())
            throw new IllegalArgumentException("Stripe checkout session id is required");
        if (!canStartStripeCheckout())
            throw new IllegalStateException("Order cannot start a Stripe checkout session");
        paymentProvider = "STRIPE";
        paymentStatus = PaymentStatus.PENDING;
        stripeCheckoutSessionId = sessionId;
        status = OrderStatus.PAYMENT_PENDING;
    }

    public void markStripePaymentPaid(String paymentIntentId, Instant paidAt) {
        paymentProvider = "STRIPE";
        paymentStatus = PaymentStatus.PAID;
        stripePaymentIntentId = paymentIntentId;
        this.paidAt = paidAt == null ? Instant.now() : paidAt;
        status = OrderStatus.PAID;
    }

    public void markStripePaymentFailed() {
        paymentProvider = "STRIPE";
        paymentStatus = PaymentStatus.FAILED;
        status = OrderStatus.PAYMENT_FAILED;
    }
}
