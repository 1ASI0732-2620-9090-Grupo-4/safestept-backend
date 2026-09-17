package com.safestep.platform.commerce.domain.model.aggregates;

import com.safestep.platform.commerce.domain.model.entities.OrderItem;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.PaymentStatus;
import org.junit.jupiter.api.Test;
import java.math.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test
    void calculatesTotalFromCopiedItemPrices() {
        var order = new Order(null, "ord-1", "ana", List.of(new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 2),
                new OrderItem("p2", "Mask", new BigDecimal("5.00"), 1)), OrderStatus.PENDING, LocalDate.now());
        assertEquals(new BigDecimal("30.00"), order.total());
    }

    @Test
    void finalTotalAppliesDiscountPercentageWithoutAffectingTotal() {
        var order = new Order(null, "ord-1", "ana",
                List.of(new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 2),
                        new OrderItem("p2", "Mask", new BigDecimal("5.00"), 1)),
                OrderStatus.PENDING, LocalDate.now(), null, PaymentStatus.NONE, null, null, null, 10, "rdc-1");

        assertEquals(new BigDecimal("30.00"), order.total());
        assertEquals(new BigDecimal("27.00"), order.finalTotal());
        assertEquals("rdc-1", order.getRedeemedCouponExternalId());
    }

    @Test
    void finalTotalEqualsTotalWithoutDiscount() {
        var order = new Order(null, "ord-1", "ana", List.of(new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 2)),
                OrderStatus.PENDING, LocalDate.now());

        assertEquals(order.total(), order.finalTotal());
        assertNull(order.getAppliedDiscountPercentage());
    }

    @Test
    void startsStripeCheckoutWithPaymentPendingState() {
        var order = new Order(null, "ord-1", "ana",
                List.of(new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 2)), OrderStatus.PENDING,
                LocalDate.now());

        order.startStripeCheckout("cs_test_123");

        assertEquals(OrderStatus.PAYMENT_PENDING, order.getStatus());
        assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
        assertEquals("STRIPE", order.getPaymentProvider());
        assertEquals("cs_test_123", order.getStripeCheckoutSessionId());
    }

    @Test
    void marksStripePaymentAsPaid() {
        var order = new Order(null, "ord-1", "ana",
                List.of(new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 2)), OrderStatus.PENDING,
                LocalDate.now());

        order.startStripeCheckout("cs_test_123");
        order.markStripePaymentPaid("pi_test_123", Instant.now());

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals("pi_test_123", order.getStripePaymentIntentId());
        assertFalse(order.canStartStripeCheckout());
    }

    @Test
    void paidOrCancelledOrdersCannotStartStripeCheckout() {
        var paidOrder = new Order(null, "ord-1", "ana",
                List.of(new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 2)), OrderStatus.PAID,
                LocalDate.now());
        var cancelledOrder = new Order(null, "ord-2", "ana",
                List.of(new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 2)), OrderStatus.CANCELLED,
                LocalDate.now());

        assertFalse(paidOrder.canStartStripeCheckout());
        assertFalse(cancelledOrder.canStartStripeCheckout());
    }
}
