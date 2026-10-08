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
import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.model.commands.CreateCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.CreateProductCommand;
import com.safestep.platform.commerce.domain.model.commands.DeleteCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.DeleteProductCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateCouponCommand;
import com.safestep.platform.commerce.domain.model.commands.UpdateProductCommand;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.domain.repositories.CouponRepository;
import com.safestep.platform.commerce.domain.repositories.OrderRepository;
import com.safestep.platform.commerce.domain.repositories.ProductRepository;
import com.safestep.platform.commerce.domain.repositories.ShoppingCartRepository;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommerceCatalogCommandServiceTest {

    private ProductRepository products;
    private CouponRepository coupons;
    private CommerceCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        coupons = mock(CouponRepository.class);
        service = new CommerceCommandServiceImpl(products, coupons, mock(ShoppingCartRepository.class),
                mock(OrderRepository.class), mock(StripeCheckoutClient.class), mock(StripeWebhookVerifier.class));
    }

    private Product product(String id, BigDecimal price) {
        return new Product(3L, id, "Product " + id, "Kits", "kit", price, null, 4.5, 10, "img.png", "Description",
                List.of());
    }

    private Coupon coupon(String id, int cost, CouponType type, int percentage, BigDecimal min) {
        return new Coupon(2L, id, "Coupon " + id, cost, type, percentage, min);
    }

    private ApplicationError errorOf(Result<?, ApplicationError> result) {
        return ((Result.Failure<?, ApplicationError>) result).error();
    }

    @Test
    @DisplayName("handle(CreateProductCommand) should save a new product and require an id (AAA)")
    void createProduct_ValidAndBlankId() {
        // Arrange
        var valid = product("kit-1", new BigDecimal("25.00"));
        when(products.existsByExternalId("kit-1")).thenReturn(false);
        when(products.save(valid)).thenReturn(valid);

        // Act
        var created = service.handle(new CreateProductCommand(valid));
        var blank = service.handle(new CreateProductCommand(product(" ", new BigDecimal("1.00"))));

        // Assert
        assertTrue(created.isSuccess());
        assertEquals("VALIDATION_ERROR", errorOf(blank).code());
        verify(products).save(valid);
    }

    @Test
    @DisplayName("handle(UpdateProductCommand) should reject blank and unknown product ids (AAA)")
    void updateProduct_BlankAndUnknownIds() {
        // Arrange
        when(products.findByExternalId("ghost")).thenReturn(Optional.empty());

        // Act + Assert
        assertEquals("VALIDATION_ERROR",
                errorOf(service.handle(new UpdateProductCommand(" ", product("x", BigDecimal.ONE)))).code());
        assertEquals("PRODUCT_NOT_FOUND",
                errorOf(service.handle(new UpdateProductCommand("ghost", product("x", BigDecimal.ONE)))).code());
    }

    @Test
    @DisplayName("handle(DeleteProductCommand) should delete an existing product (AAA)")
    void deleteProduct_ExistingProduct_IsRemoved() {
        // Arrange
        var existing = product("kit-1", new BigDecimal("25.00"));
        when(products.findByExternalId("kit-1")).thenReturn(Optional.of(existing));

        // Act
        var result = service.handle(new DeleteProductCommand("kit-1"));

        // Assert
        assertEquals("Product deleted", result.getOrElse(""));
        verify(products).delete(existing);
    }

    @Test
    @DisplayName("handle(CreateCouponCommand) should save valid simple and minimum-purchase coupons (AAA)")
    void createCoupon_ValidCoupons_AreSaved() {
        // Arrange
        var simple = coupon("cpn-5", 150, CouponType.PERCENTAGE_OFF, 5, null);
        var minimum = coupon("cpn-min", 300, CouponType.PERCENTAGE_OFF_MIN_PURCHASE, 10, new BigDecimal("150.00"));
        when(coupons.existsByExternalId(any())).thenReturn(false);
        when(coupons.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act + Assert
        assertTrue(service.handle(new CreateCouponCommand(simple)).isSuccess());
        assertTrue(service.handle(new CreateCouponCommand(minimum)).isSuccess());
    }

    @Test
    @DisplayName("handle(CreateCouponCommand) should enforce id, percentage range, minimum purchase and uniqueness (AAA)")
    void createCoupon_InvalidCoupons_AreRejected() {
        // Arrange
        when(coupons.existsByExternalId("dup")).thenReturn(true);

        // Act + Assert
        assertTrue(service.handle(new CreateCouponCommand(coupon(" ", 100, CouponType.PERCENTAGE_OFF, 5, null)))
                .isFailure());
        assertTrue(service.handle(new CreateCouponCommand(coupon("zero", 100, CouponType.PERCENTAGE_OFF, 0, null)))
                .isFailure());
        assertTrue(service.handle(new CreateCouponCommand(coupon("big", 100, CouponType.PERCENTAGE_OFF, 101, null)))
                .isFailure());
        assertTrue(service.handle(new CreateCouponCommand(
                coupon("nomin", 100, CouponType.PERCENTAGE_OFF_MIN_PURCHASE, 10, null))).isFailure());
        assertTrue(service.handle(new CreateCouponCommand(
                coupon("zeromin", 100, CouponType.PERCENTAGE_OFF_MIN_PURCHASE, 10, BigDecimal.ZERO))).isFailure());
        assertEquals("COUPON_CONFLICT",
                errorOf(service.handle(new CreateCouponCommand(coupon("dup", 100, CouponType.PERCENTAGE_OFF, 5, null))))
                        .code());
        verify(coupons, never()).save(any());
    }

    @Test
    @DisplayName("handle(UpdateCouponCommand) should keep the stored id and apply the new discount (AAA)")
    void updateCoupon_ValidUpdate_KeepsStoredId() {
        // Arrange
        when(coupons.findByExternalId("cpn-5"))
                .thenReturn(Optional.of(coupon("cpn-5", 150, CouponType.PERCENTAGE_OFF, 5, null)));
        when(coupons.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = service.handle(new UpdateCouponCommand("cpn-5",
                coupon("ignored", 200, CouponType.PERCENTAGE_OFF_MIN_PURCHASE, 12, new BigDecimal("100.00"))));

        // Assert
        var updated = result.toOptional().orElseThrow();
        assertEquals(2L, updated.getId());
        assertEquals("cpn-5", updated.getExternalId());
        assertEquals(12, updated.getDiscountPercentage());
        assertEquals(200, updated.getCostCoins());
    }

    @Test
    @DisplayName("handle(UpdateCouponCommand) should reject blank, unknown, negative-cost and invalid coupons (AAA)")
    void updateCoupon_InvalidRequests_AreRejected() {
        // Arrange
        when(coupons.findByExternalId("ghost")).thenReturn(Optional.empty());
        when(coupons.findByExternalId("cpn-5"))
                .thenReturn(Optional.of(coupon("cpn-5", 150, CouponType.PERCENTAGE_OFF, 5, null)));

        // Act + Assert
        assertTrue(service.handle(new UpdateCouponCommand(" ", coupon("x", 1, CouponType.PERCENTAGE_OFF, 5, null)))
                .isFailure());
        assertEquals("COUPON_NOT_FOUND", errorOf(service
                .handle(new UpdateCouponCommand("ghost", coupon("x", 1, CouponType.PERCENTAGE_OFF, 5, null)))).code());
        assertTrue(service.handle(new UpdateCouponCommand("cpn-5", coupon("x", -1, CouponType.PERCENTAGE_OFF, 5, null)))
                .isFailure());
        assertTrue(service.handle(new UpdateCouponCommand("cpn-5", coupon("x", 10, CouponType.PERCENTAGE_OFF, 500, null)))
                .isFailure());
        verify(coupons, never()).save(any());
    }

    @Test
    @DisplayName("handle(DeleteCouponCommand) should delete existing coupons and report unknown ones (AAA)")
    void deleteCoupon_ExistingAndUnknown() {
        // Arrange
        var existing = coupon("cpn-5", 150, CouponType.PERCENTAGE_OFF, 5, null);
        when(coupons.findByExternalId("cpn-5")).thenReturn(Optional.of(existing));
        when(coupons.findByExternalId("ghost")).thenReturn(Optional.empty());

        // Act
        var deleted = service.handle(new DeleteCouponCommand("cpn-5"));
        var missing = service.handle(new DeleteCouponCommand("ghost"));

        // Assert
        assertEquals("Coupon deleted", deleted.getOrElse(""));
        assertEquals("COUPON_NOT_FOUND", errorOf(missing).code());
        verify(coupons).delete(existing);
    }
}
