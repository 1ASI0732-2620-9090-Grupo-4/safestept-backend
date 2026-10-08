package com.safestep.platform.commerce.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeCheckoutClient;
import com.safestep.platform.commerce.application.internal.outboundservices.stripe.StripeWebhookVerifier;
import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.model.aggregates.RedeemedCoupon;
import com.safestep.platform.commerce.domain.model.commands.AddCartItemCommand;
import com.safestep.platform.commerce.domain.model.commands.CancelStripePaymentCommand;
import com.safestep.platform.commerce.domain.model.commands.CaptureStripeWebhookCommand;
import com.safestep.platform.commerce.domain.model.commands.ConfirmStripePaymentCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateOrderCommand;
import com.safestep.platform.commerce.domain.model.commands.RedeemCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateCartItemCommand;
import com.safestep.platform.commerce.domain.model.entities.CartItem;
import com.safestep.platform.commerce.domain.model.entities.OrderItem;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.PaymentStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.RedemptionStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.StripePaymentConfirmation;
import com.safestep.platform.commerce.domain.model.valueobjects.StripeWebhookEvent;
import com.safestep.platform.commerce.domain.repositories.CouponRepository;
import com.safestep.platform.commerce.domain.repositories.OrderRepository;
import com.safestep.platform.commerce.domain.repositories.ProductRepository;
import com.safestep.platform.commerce.domain.repositories.RedeemedCouponRepository;
import com.safestep.platform.commerce.domain.repositories.ShoppingCartRepository;
import com.safestep.platform.gamification.interfaces.acl.GamificationContextFacade;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommerceCartAndOrderCommandServiceTest {

    private ProductRepository products;
    private CouponRepository coupons;
    private RedeemedCouponRepository redeemedCoupons;
    private ShoppingCartRepository carts;
    private OrderRepository orders;
    private StripeCheckoutClient stripe;
    private StripeWebhookVerifier verifier;
    private CommerceCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        coupons = mock(CouponRepository.class);
        redeemedCoupons = mock(RedeemedCouponRepository.class);
        carts = mock(ShoppingCartRepository.class);
        orders = mock(OrderRepository.class);
        stripe = mock(StripeCheckoutClient.class);
        verifier = mock(StripeWebhookVerifier.class);
        service = new CommerceCommandServiceImpl(products, coupons, redeemedCoupons, carts, orders, stripe, verifier,
                mock(GamificationContextFacade.class));
    }

    private Product product(String id, int stock) {
        return new Product(1L, id, "Product " + id, "Kits", "kit", new BigDecimal("50.00"), null, 4.5, stock,
                "img.png", "Description", List.of());
    }

    private Order pendingOrder(String username) {
        return new Order(1L, "ord-1", username, List.of(new OrderItem("p1", "Bandage", new BigDecimal("10.00"), 2)),
                OrderStatus.PENDING, LocalDate.now());
    }

    private ApplicationError errorOf(Result<?, ApplicationError> result) {
        return ((Result.Failure<?, ApplicationError>) result).error();
    }

    @Test
    @DisplayName("handle(AddCartItemCommand) should add a valid item to the cart (AAA)")
    void addCartItem_ValidItem_SavesIt() {
        // Arrange
        when(products.findByExternalId("p1")).thenReturn(Optional.of(product("p1", 5)));
        when(carts.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = service.handle(new AddCartItemCommand("ana", "p1", 2));

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.toOptional().orElseThrow().getQuantity());
    }

    @Test
    @DisplayName("handle(AddCartItemCommand) should reject unknown products and quantities above the stock (AAA)")
    void addCartItem_InvalidRequests_AreRejected() {
        // Arrange
        when(products.findByExternalId("p1")).thenReturn(Optional.of(product("p1", 3)));
        when(products.findByExternalId("ghost")).thenReturn(Optional.empty());

        // Act + Assert
        assertEquals("PRODUCT_NOT_FOUND", errorOf(service.handle(new AddCartItemCommand("ana", "ghost", 1))).code());
        assertTrue(service.handle(new AddCartItemCommand("ana", "p1", 0)).isFailure());
        assertTrue(service.handle(new AddCartItemCommand("ana", "p1", 4)).isFailure());
        verify(carts, never()).save(any());
    }

    @Test
    @DisplayName("handle(UpdateCartItemCommand) should change the quantity of an existing item (AAA)")
    void updateCartItem_ValidQuantity_UpdatesItem() {
        // Arrange
        var item = new CartItem(1L, "cart-1", "ana", "p1", 1, Instant.now());
        when(carts.findByExternalIdAndUsername("cart-1", "ana")).thenReturn(Optional.of(item));
        when(products.findByExternalId("p1")).thenReturn(Optional.of(product("p1", 5)));
        when(carts.save(item)).thenReturn(item);

        // Act
        var result = service.handle(new UpdateCartItemCommand("ana", "cart-1", 4));

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(4, item.getQuantity());
    }

    @Test
    @DisplayName("handle(UpdateCartItemCommand) should reject missing items and quantities above the stock (AAA)")
    void updateCartItem_InvalidRequests_AreRejected() {
        // Arrange
        var item = new CartItem(1L, "cart-1", "ana", "p1", 1, Instant.now());
        when(carts.findByExternalIdAndUsername("cart-1", "ana")).thenReturn(Optional.of(item));
        when(carts.findByExternalIdAndUsername("ghost", "ana")).thenReturn(Optional.empty());
        when(products.findByExternalId("p1")).thenReturn(Optional.of(product("p1", 2)));

        // Act + Assert
        assertEquals("CART ITEM_NOT_FOUND", errorOf(service.handle(new UpdateCartItemCommand("ana", "ghost", 1))).code());
        assertTrue(service.handle(new UpdateCartItemCommand("ana", "cart-1", 9)).isFailure());
        assertEquals(1, item.getQuantity());
    }

    @Test
    @DisplayName("deleteCartItem should remove the item only when it belongs to the user (AAA)")
    void deleteCartItem_RemovesOwnedItem() {
        // Arrange
        var item = new CartItem(1L, "cart-1", "ana", "p1", 1, Instant.now());
        when(carts.findByExternalIdAndUsername("cart-1", "ana")).thenReturn(Optional.of(item));
        when(carts.findByExternalIdAndUsername("cart-9", "ana")).thenReturn(Optional.empty());

        // Act
        service.deleteCartItem("ana", "cart-1");
        service.deleteCartItem("ana", "cart-9");

        // Assert
        verify(carts).delete(item);
    }

    @Test
    @DisplayName("handle(CreateOrderCommand) should fail when the cart is empty (AAA)")
    void createOrder_EmptyCart_Fails() {
        // Arrange
        when(carts.findByUsername("ana")).thenReturn(List.of());

        // Act
        var result = service.handle(new CreateOrderCommand("ana", "PENDING", null));

        // Assert
        assertEquals("BUSINESS_RULE_VIOLATION", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(CreateOrderCommand) should fail when a cart product no longer exists or lacks stock (AAA)")
    void createOrder_MissingProductOrStock_Fails() {
        // Arrange
        var line = new CartItem(1L, "cart-1", "ana", "p1", 3, Instant.now());
        when(carts.findByUsername("ana")).thenReturn(List.of(line));
        when(products.findByExternalId("p1")).thenReturn(Optional.empty(), Optional.of(product("p1", 1)));

        // Act
        var missing = service.handle(new CreateOrderCommand("ana", "PENDING", null));
        var noStock = service.handle(new CreateOrderCommand("ana", "PENDING", null));

        // Assert
        assertEquals("PRODUCT_NOT_FOUND", errorOf(missing).code());
        assertEquals("BUSINESS_RULE_VIOLATION", errorOf(noStock).code());
    }

    @Test
    @DisplayName("handle(CreateOrderCommand) should create the order, reduce stock and empty the cart (AAA)")
    void createOrder_WithoutCoupon_CreatesOrder() {
        // Arrange
        var stocked = product("p1", 10);
        when(carts.findByUsername("ana")).thenReturn(List.of(new CartItem(1L, "cart-1", "ana", "p1", 2, Instant.now())));
        when(products.findByExternalId("p1")).thenReturn(Optional.of(stocked));

        // Act
        var result = service.handle(new CreateOrderCommand("ana", "PENDING", null));

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("100.00"), result.toOptional().orElseThrow().total());
        assertEquals(8, stocked.getStock());
        verify(orders).save(any(Order.class));
        verify(carts).deleteByUsername("ana");
        verify(redeemedCoupons, never()).save(any());
    }

    @Test
    @DisplayName("handle(CreateOrderCommand) should reject unknown, foreign or already used coupons (AAA)")
    void createOrder_InvalidCoupons_AreRejected() {
        // Arrange
        when(carts.findByUsername("ana")).thenReturn(List.of(new CartItem(1L, "cart-1", "ana", "p1", 1, Instant.now())));
        when(products.findByExternalId("p1")).thenReturn(Optional.of(product("p1", 10)));
        when(redeemedCoupons.findByExternalId("ghost")).thenReturn(Optional.empty());
        when(redeemedCoupons.findByExternalId("foreign")).thenReturn(Optional.of(new RedeemedCoupon(1L, "foreign",
                "luis", "cpn", "T", CouponType.PERCENTAGE_OFF, 10, null, Instant.now(), null,
                RedemptionStatus.AVAILABLE)));
        when(redeemedCoupons.findByExternalId("used")).thenReturn(Optional.of(new RedeemedCoupon(2L, "used", "ana",
                "cpn", "T", CouponType.PERCENTAGE_OFF, 10, null, Instant.now(), Instant.now(),
                RedemptionStatus.USED)));

        // Act + Assert
        assertEquals("REDEEMED COUPON_NOT_FOUND", errorOf(service.handle(new CreateOrderCommand("ana", "PENDING", "ghost"))).code());
        assertTrue(errorOf(service.handle(new CreateOrderCommand("ana", "PENDING", "foreign"))).message()
                .contains("coupon ownership"));
        assertTrue(service.handle(new CreateOrderCommand("ana", "PENDING", "used")).isFailure());
    }

    @Test
    @DisplayName("handle(CreateStripeCheckoutSessionCommand) should reject paid orders and Stripe failures (AAA)")
    void createCheckoutSession_RejectsInvalidStates() {
        // Arrange
        var paid = pendingOrder("ana");
        paid.markStripePaymentPaid("pi_1", Instant.now());
        when(orders.findByExternalId("ord-paid")).thenReturn(Optional.of(paid));
        when(orders.findByExternalId("ghost")).thenReturn(Optional.empty());
        var pending = pendingOrder("ana");
        when(orders.findByExternalId("ord-1")).thenReturn(Optional.of(pending));
        when(stripe.createCheckoutSession(pending))
                .thenReturn(Result.failure(ApplicationError.unexpected("stripe", "down")));

        // Act + Assert
        var create = new com.safestep.platform.commerce.domain.model.commands.CreateStripeCheckoutSessionCommand("ana",
                "ghost");
        assertEquals("ORDER_NOT_FOUND", errorOf(service.handle(create)).code());
        assertTrue(service.handle(new com.safestep.platform.commerce.domain.model.commands.CreateStripeCheckoutSessionCommand(
                "ana", "ord-paid")).isFailure());
        assertEquals("UNEXPECTED_ERROR", errorOf(service.handle(
                new com.safestep.platform.commerce.domain.model.commands.CreateStripeCheckoutSessionCommand("ana",
                        "ord-1"))).code());
    }

    @Test
    @DisplayName("handle(ConfirmStripePaymentCommand) should mark the order as paid when Stripe confirms (AAA)")
    void confirmPayment_PaidSession_MarksOrderPaid() {
        // Arrange
        var order = pendingOrder("ana");
        order.startStripeCheckout("cs_1");
        when(orders.findByExternalId("ord-1")).thenReturn(Optional.of(order));
        when(orders.save(order)).thenReturn(order);
        when(stripe.retrieveCheckoutSession("cs_1"))
                .thenReturn(Result.success(new StripePaymentConfirmation("cs_1", "paid", "pi_1")));

        // Act
        var result = service.handle(new ConfirmStripePaymentCommand("ana", "ord-1", "cs_1"));

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("pi_1", order.getStripePaymentIntentId());
    }

    @Test
    @DisplayName("handle(ConfirmStripePaymentCommand) should reject foreign orders, other sessions and unpaid sessions (AAA)")
    void confirmPayment_InvalidRequests_AreRejected() {
        // Arrange
        var order = pendingOrder("ana");
        order.startStripeCheckout("cs_1");
        when(orders.findByExternalId("ord-1")).thenReturn(Optional.of(order));
        when(orders.findByExternalId("ghost")).thenReturn(Optional.empty());
        when(stripe.retrieveCheckoutSession("cs_1")).thenReturn(
                Result.success(new StripePaymentConfirmation("cs_1", "unpaid", null)),
                Result.failure(ApplicationError.unexpected("stripe", "down")));

        // Act + Assert
        assertEquals("ORDER_NOT_FOUND", errorOf(service.handle(new ConfirmStripePaymentCommand("ana", "ghost", "cs_1"))).code());
        assertTrue(service.handle(new ConfirmStripePaymentCommand("luis", "ord-1", "cs_1")).isFailure());
        assertTrue(service.handle(new ConfirmStripePaymentCommand("ana", "ord-1", "cs_other")).isFailure());
        assertTrue(service.handle(new ConfirmStripePaymentCommand("ana", "ord-1", "cs_1")).isFailure());
        assertEquals("UNEXPECTED_ERROR", errorOf(service.handle(new ConfirmStripePaymentCommand("ana", "ord-1", "cs_1"))).code());
        assertEquals(OrderStatus.PAYMENT_PENDING, order.getStatus());
    }

    @Test
    @DisplayName("handle(CancelStripePaymentCommand) should fail the payment and release the redeemed coupon (AAA)")
    void cancelPayment_ReleasesCoupon_AndValidatesOwnership() {
        // Arrange
        var order = new Order(1L, "ord-1", "ana", List.of(new OrderItem("p1", "Bandage", new BigDecimal("10.00"), 1)),
                OrderStatus.PENDING, LocalDate.now(), null, PaymentStatus.NONE, null, null, null, 10, "rdc-1");
        order.startStripeCheckout("cs_1");
        var used = new RedeemedCoupon(1L, "rdc-1", "ana", "cpn", "T", CouponType.PERCENTAGE_OFF, 10, null,
                Instant.now(), Instant.now(), RedemptionStatus.USED);
        when(orders.findByExternalId("ord-1")).thenReturn(Optional.of(order));
        when(orders.findByExternalId("ghost")).thenReturn(Optional.empty());
        when(redeemedCoupons.findByExternalId("rdc-1")).thenReturn(Optional.of(used));
        when(orders.save(order)).thenReturn(order);

        // Act + Assert
        assertEquals("ORDER_NOT_FOUND", errorOf(service.handle(new CancelStripePaymentCommand("ana", "ghost", null))).code());
        assertTrue(service.handle(new CancelStripePaymentCommand("luis", "ord-1", null)).isFailure());
        assertTrue(service.handle(new CancelStripePaymentCommand("ana", "ord-1", "cs_other")).isFailure());
        assertTrue(service.handle(new CancelStripePaymentCommand("ana", "ord-1", "cs_1")).isSuccess());
        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        assertEquals(RedemptionStatus.AVAILABLE, used.getStatus());
    }

    @Test
    @DisplayName("handle(CaptureStripeWebhookCommand) should react to completed and expired sessions (AAA)")
    void webhook_CompletedAndExpired_UpdateOrders() {
        // Arrange
        var completed = pendingOrder("ana");
        completed.startStripeCheckout("cs_ok");
        var expired = pendingOrder("ana");
        expired.startStripeCheckout("cs_old");
        when(verifier.verify("p-ok", "sig")).thenReturn(Result.success(new StripeWebhookEvent("checkout.session.completed", "cs_ok", "pi_9")));
        when(verifier.verify("p-old", "sig")).thenReturn(Result.success(new StripeWebhookEvent("checkout.session.expired", "cs_old", null)));
        when(verifier.verify("p-ignored", "sig")).thenReturn(Result.success(new StripeWebhookEvent("customer.created", "x", null)));
        when(verifier.verify("p-ghost", "sig")).thenReturn(Result.success(new StripeWebhookEvent("checkout.session.completed", "cs_ghost", null)));
        when(orders.findByStripeCheckoutSessionId("cs_ok")).thenReturn(Optional.of(completed));
        when(orders.findByStripeCheckoutSessionId("cs_old")).thenReturn(Optional.of(expired));
        when(orders.findByStripeCheckoutSessionId("cs_ghost")).thenReturn(Optional.empty());

        // Act
        var ok = service.handle(new CaptureStripeWebhookCommand("p-ok", "sig"));
        var old = service.handle(new CaptureStripeWebhookCommand("p-old", "sig"));
        var ignored = service.handle(new CaptureStripeWebhookCommand("p-ignored", "sig"));
        var ghost = service.handle(new CaptureStripeWebhookCommand("p-ghost", "sig"));

        // Assert
        assertEquals("Webhook captured", ok.getOrElse(""));
        assertEquals("Webhook captured", old.getOrElse(""));
        assertEquals("Event ignored", ignored.getOrElse(""));
        assertTrue(ghost.isFailure());
        assertEquals(OrderStatus.PAID, completed.getStatus());
        assertEquals(OrderStatus.PAYMENT_FAILED, expired.getStatus());
    }

    @Test
    @DisplayName("handle(RedeemCouponCommand) should fail for an unknown coupon without touching the wallet (AAA)")
    void redeemCoupon_UnknownCoupon_Fails() {
        // Arrange
        when(coupons.findByExternalId("ghost")).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new RedeemCouponCommand("ana", "ghost"));

        // Assert
        assertEquals("COUPON_NOT_FOUND", errorOf(result).code());
    }
}
