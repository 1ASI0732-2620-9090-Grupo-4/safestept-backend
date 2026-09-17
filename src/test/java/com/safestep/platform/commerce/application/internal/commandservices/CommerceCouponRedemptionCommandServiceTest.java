package com.safestep.platform.commerce.application.internal.commandservices;

import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeCheckoutClient;
import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeWebhookVerifier;
import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.model.aggregates.RedeemedCoupon;
import com.safestep.platform.commerce.domain.model.commands.CancelStripePaymentCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateOrderCommand;
import com.safestep.platform.commerce.domain.model.commands.RedeemCouponCommand;
import com.safestep.platform.commerce.domain.model.entities.CartItem;
import com.safestep.platform.commerce.domain.model.entities.OrderItem;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.RedemptionStatus;
import com.safestep.platform.commerce.domain.repositories.CouponRepository;
import com.safestep.platform.commerce.domain.repositories.OrderRepository;
import com.safestep.platform.commerce.domain.repositories.ProductRepository;
import com.safestep.platform.commerce.domain.repositories.RedeemedCouponRepository;
import com.safestep.platform.commerce.domain.repositories.ShoppingCartRepository;
import com.safestep.platform.gamification.interfaces.acl.GamificationContextFacade;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommerceCouponRedemptionCommandServiceTest {

    @Test
    void redeemCouponSucceedsAndSpendsCoins() {
        var coupons = mock(CouponRepository.class);
        var redeemedCoupons = mock(RedeemedCouponRepository.class);
        var gamification = mock(GamificationContextFacade.class);
        when(coupons.findByExternalId("cpn-5"))
                .thenReturn(Optional.of(coupon("cpn-5", "5% off", 150, CouponType.PERCENTAGE_OFF, 5, null)));
        when(gamification.spendCoins("ana", 150, "cpn-5", "5% off")).thenReturn(true);
        when(redeemedCoupons.save(any(RedeemedCoupon.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var service = service(coupons, redeemedCoupons, gamification);

        var result = service.handle(new RedeemCouponCommand("ana", "cpn-5"));

        assertTrue(result.isSuccess());
        var saved = ArgumentCaptor.forClass(RedeemedCoupon.class);
        verify(redeemedCoupons).save(saved.capture());
        assertEquals(RedemptionStatus.AVAILABLE, saved.getValue().getStatus());
        assertEquals("ana", saved.getValue().getUsername());
    }

    @Test
    void redeemCouponFailsWhenCoinsAreInsufficient() {
        var coupons = mock(CouponRepository.class);
        var redeemedCoupons = mock(RedeemedCouponRepository.class);
        var gamification = mock(GamificationContextFacade.class);
        when(coupons.findByExternalId("cpn-5"))
                .thenReturn(Optional.of(coupon("cpn-5", "5% off", 150, CouponType.PERCENTAGE_OFF, 5, null)));
        when(gamification.spendCoins("ana", 150, "cpn-5", "5% off")).thenReturn(false);
        var service = service(coupons, redeemedCoupons, gamification);

        var result = service.handle(new RedeemCouponCommand("ana", "cpn-5"));

        assertTrue(result.isFailure());
        verify(redeemedCoupons, never()).save(any());
    }

    @Test
    void createOrderRejectsCouponWhenMinimumPurchaseIsNotReached() {
        var products = mock(ProductRepository.class);
        var carts = mock(ShoppingCartRepository.class);
        var redeemedCoupons = mock(RedeemedCouponRepository.class);
        when(carts.findByUsername("ana")).thenReturn(List.of(cartItem("p1", 1)));
        when(products.findByExternalId("p1")).thenReturn(Optional.of(product("p1", new BigDecimal("50.00"))));
        when(redeemedCoupons.findByExternalId("rdc-1")).thenReturn(Optional.of(
                redeemedCoupon("rdc-1", "ana", CouponType.PERCENTAGE_OFF_MIN_PURCHASE, 15, new BigDecimal("100.00"))));
        var service = service(products, carts, redeemedCoupons);

        var result = service.handle(new CreateOrderCommand("ana", "PENDING", "rdc-1"));

        assertTrue(result.isFailure());
        verify(redeemedCoupons, never()).save(any());
    }

    @Test
    void createOrderAppliesDiscountAndMarksCouponUsed() {
        var products = mock(ProductRepository.class);
        var carts = mock(ShoppingCartRepository.class);
        var orders = mock(OrderRepository.class);
        var redeemedCoupons = mock(RedeemedCouponRepository.class);
        when(carts.findByUsername("ana")).thenReturn(List.of(cartItem("p1", 2)));
        when(products.findByExternalId("p1")).thenReturn(Optional.of(product("p1", new BigDecimal("100.00"))));
        when(redeemedCoupons.findByExternalId("rdc-1")).thenReturn(Optional.of(
                redeemedCoupon("rdc-1", "ana", CouponType.PERCENTAGE_OFF_MIN_PURCHASE, 10, new BigDecimal("100.00"))));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var service = service(products, carts, orders, redeemedCoupons);

        var result = service.handle(new CreateOrderCommand("ana", "PENDING", "rdc-1"));

        assertTrue(result.isSuccess());
        var order = result.toOptional().orElseThrow();
        assertEquals(new BigDecimal("200.00"), order.total());
        assertEquals(new BigDecimal("180.00"), order.finalTotal());
        var saved = ArgumentCaptor.forClass(RedeemedCoupon.class);
        verify(redeemedCoupons).save(saved.capture());
        assertEquals(RedemptionStatus.USED, saved.getValue().getStatus());
    }

    @Test
    void cancellingStripePaymentReleasesRedeemedCoupon() {
        var orders = mock(OrderRepository.class);
        var stripe = mock(StripeCheckoutClient.class);
        var verifier = mock(StripeWebhookVerifier.class);
        var redeemedCoupons = mock(RedeemedCouponRepository.class);
        var order = orderWithCoupon("ana", "rdc-1");
        when(orders.findByExternalId("ord-1")).thenReturn(Optional.of(order));
        when(redeemedCoupons.findByExternalId("rdc-1")).thenReturn(Optional.of(
                usedRedeemedCoupon("rdc-1", "ana")));
        var service = service(orders, stripe, verifier, redeemedCoupons);

        var result = service.handle(new CancelStripePaymentCommand("ana", "ord-1", null));

        assertTrue(result.isSuccess());
        var saved = ArgumentCaptor.forClass(RedeemedCoupon.class);
        verify(redeemedCoupons).save(saved.capture());
        assertEquals(RedemptionStatus.AVAILABLE, saved.getValue().getStatus());
    }

    private CommerceCommandServiceImpl service(CouponRepository coupons, RedeemedCouponRepository redeemedCoupons,
            GamificationContextFacade gamification) {
        return new CommerceCommandServiceImpl(mock(ProductRepository.class), coupons, redeemedCoupons,
                mock(ShoppingCartRepository.class), mock(OrderRepository.class), mock(StripeCheckoutClient.class),
                mock(StripeWebhookVerifier.class), gamification);
    }

    private CommerceCommandServiceImpl service(ProductRepository products, ShoppingCartRepository carts,
            RedeemedCouponRepository redeemedCoupons) {
        return service(products, carts, mock(OrderRepository.class), redeemedCoupons);
    }

    private CommerceCommandServiceImpl service(ProductRepository products, ShoppingCartRepository carts,
            OrderRepository orders, RedeemedCouponRepository redeemedCoupons) {
        return new CommerceCommandServiceImpl(products, mock(CouponRepository.class), redeemedCoupons, carts, orders,
                mock(StripeCheckoutClient.class), mock(StripeWebhookVerifier.class),
                mock(GamificationContextFacade.class));
    }

    private CommerceCommandServiceImpl service(OrderRepository orders, StripeCheckoutClient stripe,
            StripeWebhookVerifier verifier, RedeemedCouponRepository redeemedCoupons) {
        return new CommerceCommandServiceImpl(mock(ProductRepository.class), mock(CouponRepository.class),
                redeemedCoupons, mock(ShoppingCartRepository.class), orders, stripe, verifier,
                mock(GamificationContextFacade.class));
    }

    private Coupon coupon(String id, String title, int costCoins, CouponType type, int discountPercentage,
            BigDecimal minPurchaseAmount) {
        return new Coupon(1L, id, title, costCoins, type, discountPercentage, minPurchaseAmount);
    }

    private RedeemedCoupon redeemedCoupon(String externalId, String username, CouponType type,
            int discountPercentage, BigDecimal minPurchaseAmount) {
        return new RedeemedCoupon(1L, externalId, username, "cpn-1", "Coupon", type, discountPercentage,
                minPurchaseAmount, Instant.now(), null, RedemptionStatus.AVAILABLE);
    }

    private RedeemedCoupon usedRedeemedCoupon(String externalId, String username) {
        return new RedeemedCoupon(1L, externalId, username, "cpn-1", "Coupon", CouponType.PERCENTAGE_OFF, 10, null,
                Instant.now(), Instant.now(), RedemptionStatus.USED);
    }

    private CartItem cartItem(String productId, int quantity) {
        return new CartItem(1L, "cart-1", "ana", productId, quantity, Instant.now());
    }

    private Product product(String externalId, BigDecimal price) {
        return new Product(1L, externalId, "Product", "Kits", "kit", price, null, 4.5, 100, "image.png",
                "Description", List.of());
    }

    private Order orderWithCoupon(String username, String redeemedCouponExternalId) {
        var order = new Order(1L, "ord-1", username,
                List.of(new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 2)), OrderStatus.PENDING,
                java.time.LocalDate.now(), null,
                com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.PaymentStatus.NONE,
                null, null, null, 10, redeemedCouponExternalId);
        order.startStripeCheckout("cs_test_123");
        return order;
    }
}
