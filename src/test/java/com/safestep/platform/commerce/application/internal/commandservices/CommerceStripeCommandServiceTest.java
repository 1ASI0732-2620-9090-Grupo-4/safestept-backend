package com.safestep.platform.commerce.application.internal.commandservices;

import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeCheckoutClient;
import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeWebhookVerifier;
import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.model.commands.CaptureStripeWebhookCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateProductCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateStripeCheckoutSessionCommand;
import com.safestep.platform.commerce.domain.model.commands.DeleteProductCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateProductCommand;
import com.safestep.platform.commerce.domain.model.entities.OrderItem;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.PaymentStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.StripeCheckoutSession;
import com.safestep.platform.commerce.domain.model.valueobjects.StripeWebhookEvent;
import com.safestep.platform.commerce.domain.repositories.CouponRepository;
import com.safestep.platform.commerce.domain.repositories.OrderRepository;
import com.safestep.platform.commerce.domain.repositories.ProductRepository;
import com.safestep.platform.commerce.domain.repositories.ShoppingCartRepository;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommerceStripeCommandServiceTest {

    @Test
    void createStripeCheckoutSessionUsesPersistedOrderTotalAndMarksPending() {
        var order = order("ana", OrderStatus.PENDING);
        var orders = mock(OrderRepository.class);
        var stripe = mock(StripeCheckoutClient.class);
        var verifier = mock(StripeWebhookVerifier.class);
        when(orders.findByExternalId("ord-1")).thenReturn(Optional.of(order));
        when(stripe.createCheckoutSession(order))
                .thenReturn(Result.success(new StripeCheckoutSession("cs_test_123", "https://checkout.stripe.test")));
        var service = service(orders, stripe, verifier);

        var result = service.handle(new CreateStripeCheckoutSessionCommand("ana", "ord-1"));

        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("30.00"), order.total());
        assertEquals(OrderStatus.PAYMENT_PENDING, order.getStatus());
        assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
        assertEquals("cs_test_123", order.getStripeCheckoutSessionId());
        verify(orders).save(order);
    }

    @Test
    void createStripeCheckoutSessionRejectsForeignOrder() {
        var order = order("ana", OrderStatus.PENDING);
        var orders = mock(OrderRepository.class);
        var stripe = mock(StripeCheckoutClient.class);
        var verifier = mock(StripeWebhookVerifier.class);
        when(orders.findByExternalId("ord-1")).thenReturn(Optional.of(order));
        var service = service(orders, stripe, verifier);

        var result = service.handle(new CreateStripeCheckoutSessionCommand("luis", "ord-1"));

        assertTrue(result.isFailure());
        verifyNoInteractions(stripe);
    }

    @Test
    void completedWebhookMarksOrderAsPaid() {
        var order = order("ana", OrderStatus.PAYMENT_PENDING);
        order.startStripeCheckout("cs_test_123");
        var orders = mock(OrderRepository.class);
        var stripe = mock(StripeCheckoutClient.class);
        var verifier = mock(StripeWebhookVerifier.class);
        when(verifier.verify("payload", "signature"))
                .thenReturn(Result.success(new StripeWebhookEvent("checkout.session.completed", "cs_test_123",
                        "pi_test_123")));
        when(orders.findByStripeCheckoutSessionId("cs_test_123")).thenReturn(Optional.of(order));
        var service = service(orders, stripe, verifier);

        var result = service.handle(new CaptureStripeWebhookCommand("payload", "signature"));

        assertTrue(result.isSuccess());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals("pi_test_123", order.getStripePaymentIntentId());
        verify(orders).save(order);
    }

    @Test
    void duplicateCompletedWebhookKeepsPaidOrderPaid() {
        var order = order("ana", OrderStatus.PAYMENT_PENDING);
        order.startStripeCheckout("cs_test_123");
        order.markStripePaymentPaid("pi_test_123", java.time.Instant.now());
        var orders = mock(OrderRepository.class);
        var stripe = mock(StripeCheckoutClient.class);
        var verifier = mock(StripeWebhookVerifier.class);
        when(verifier.verify("payload", "signature"))
                .thenReturn(Result.success(new StripeWebhookEvent("checkout.session.completed", "cs_test_123",
                        "pi_test_123")));
        when(orders.findByStripeCheckoutSessionId("cs_test_123")).thenReturn(Optional.of(order));
        var service = service(orders, stripe, verifier);

        var result = service.handle(new CaptureStripeWebhookCommand("payload", "signature"));

        assertTrue(result.isSuccess());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
    }

    @Test
    void invalidWebhookSignatureReturnsValidationFailure() {
        var orders = mock(OrderRepository.class);
        var stripe = mock(StripeCheckoutClient.class);
        var verifier = mock(StripeWebhookVerifier.class);
        when(verifier.verify("payload", "bad")).thenReturn(Result.failure(
                ApplicationError.validationError("stripeSignature", "Invalid Stripe webhook signature")));
        var service = service(orders, stripe, verifier);

        var result = service.handle(new CaptureStripeWebhookCommand("payload", "bad"));

        assertTrue(result.isFailure());
        verifyNoInteractions(orders);
    }

    @Test
    void createProductRejectsDuplicatedExternalId() {
        var products = mock(ProductRepository.class);
        var service = service(products, mock(CouponRepository.class));
        when(products.existsByExternalId("kit-basic")).thenReturn(true);

        var result = service.handle(new CreateProductCommand(product("kit-basic", "Basic Kit")));

        assertTrue(result.isFailure());
        verify(products, never()).save(any());
    }

    @Test
    void updateProductKeepsDatabaseIdAndPathExternalId() {
        var products = mock(ProductRepository.class);
        var service = service(products, mock(CouponRepository.class));
        when(products.findByExternalId("kit-basic")).thenReturn(Optional.of(productWithId(9L, "kit-basic", "Old")));
        when(products.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new UpdateProductCommand("kit-basic", product("ignored", "Updated Kit")));

        assertTrue(result.isSuccess());
        var saved = ArgumentCaptor.forClass(Product.class);
        verify(products).save(saved.capture());
        assertEquals(9L, saved.getValue().getId());
        assertEquals("kit-basic", saved.getValue().getExternalId());
        assertEquals("Updated Kit", saved.getValue().getName());
    }

    @Test
    void deleteProductReturnsNotFoundWhenMissing() {
        var products = mock(ProductRepository.class);
        var service = service(products, mock(CouponRepository.class));
        when(products.findByExternalId("missing")).thenReturn(Optional.empty());

        var result = service.handle(new DeleteProductCommand("missing"));

        assertTrue(result.isFailure());
        verify(products, never()).delete(any());
    }

    @Test
    void createCouponRejectsNegativeCost() {
        var coupons = mock(CouponRepository.class);
        var service = service(mock(ProductRepository.class), coupons);

        var result = service.handle(new CreateCouponCommand(new Coupon(null, "coupon-1", "Coupon", -1, "10%")));

        assertTrue(result.isFailure());
        verify(coupons, never()).save(any());
    }

    private CommerceCommandServiceImpl service(OrderRepository orders, StripeCheckoutClient stripe,
            StripeWebhookVerifier verifier) {
        return new CommerceCommandServiceImpl(mock(ProductRepository.class), mock(ShoppingCartRepository.class),
                orders, stripe, verifier);
    }

    private CommerceCommandServiceImpl service(ProductRepository products, CouponRepository coupons) {
        return new CommerceCommandServiceImpl(products, coupons, mock(ShoppingCartRepository.class),
                mock(OrderRepository.class), mock(StripeCheckoutClient.class), mock(StripeWebhookVerifier.class));
    }

    private Order order(String username, OrderStatus status) {
        return new Order(null, "ord-1", username,
                List.of(new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 2),
                        new OrderItem("p2", "Mask", new BigDecimal("5.00"), 1)),
                status, LocalDate.now());
    }

    private Product product(String externalId, String name) {
        return productWithId(null, externalId, name);
    }

    private Product productWithId(Long id, String externalId, String name) {
        return new Product(id, externalId, name, "Kits", "kit", new BigDecimal("25.00"), null, 4.5, 12,
                "image.png", "Emergency product", List.of("first-aid"));
    }
}
