package com.safestep.platform.commerce.interfaces.rest.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import com.safestep.platform.commerce.domain.model.aggregates.Order;
import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.model.aggregates.RedeemedCoupon;
import com.safestep.platform.commerce.domain.model.entities.CartItem;
import com.safestep.platform.commerce.domain.model.entities.OrderItem;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.PaymentStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.RedemptionStatus;
import com.safestep.platform.commerce.interfaces.rest.resources.CouponResource;
import com.safestep.platform.commerce.interfaces.rest.resources.ProductResource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommerceResourceAssemblerTest {

    @Test
    @DisplayName("product mapping should round trip price, stock and tags (AAA)")
    void product_RoundTrip() {
        // Arrange
        var product = new Product(1L, "kit-1", "Basic Kit", "Kits", "kit", new BigDecimal("25.00"),
                new BigDecimal("30.00"), 4.5, 12, "kit.png", "Emergency kit", List.of("first-aid"));

        // Act
        var resource = CommerceResourceAssembler.toResource(product);
        var rebuilt = CommerceResourceAssembler.toProduct(new ProductResource("kit-2", "Other", "Kits", "kit",
                new BigDecimal("10.00"), null, 4.0, 3, "o.png", List.of("tag"), "Desc"));

        // Assert
        assertEquals("kit-1", resource.id());
        assertEquals(new BigDecimal("25.00"), resource.price());
        assertEquals(12, resource.stock());
        assertEquals(List.of("first-aid"), resource.tags());
        assertEquals("kit-2", rebuilt.getExternalId());
        assertNull(rebuilt.getOldPrice());
        assertEquals(3, rebuilt.getStock());
    }

    @Test
    @DisplayName("coupon mapping should keep type, percentage and minimum purchase (AAA)")
    void coupon_RoundTrip() {
        // Arrange
        var coupon = new Coupon(1L, "cpn-min-10", "10% over S/150", 300, CouponType.PERCENTAGE_OFF_MIN_PURCHASE, 10,
                new BigDecimal("150.00"));

        // Act
        var resource = CommerceResourceAssembler.toResource(coupon);
        var rebuilt = CommerceResourceAssembler.toCoupon(
                new CouponResource("cpn-5", "5% off", 150, "PERCENTAGE_OFF", 5, new BigDecimal("999.00")));

        // Assert
        assertEquals("PERCENTAGE_OFF_MIN_PURCHASE", resource.type());
        assertEquals(10, resource.discountPercentage());
        assertEquals(new BigDecimal("150.00"), resource.minPurchaseAmount());
        assertEquals(CouponType.PERCENTAGE_OFF, rebuilt.getType());
        assertNull(rebuilt.getMinPurchaseAmount(), "A simple percentage coupon must drop any minimum purchase");
    }

    @Test
    @DisplayName("toResource(RedeemedCoupon) should expose status and snapshot data (AAA)")
    void redeemedCoupon_MapsSnapshot() {
        // Arrange
        var at = Instant.parse("2026-09-14T15:00:00Z");
        var redeemed = new RedeemedCoupon(1L, "rdc-1", "ana", "cpn-5", "5% off", CouponType.PERCENTAGE_OFF, 5, null,
                at, null, RedemptionStatus.AVAILABLE);

        // Act
        var resource = CommerceResourceAssembler.toResource(redeemed);

        // Assert
        assertEquals("rdc-1", resource.id());
        assertEquals("cpn-5", resource.couponId());
        assertEquals("AVAILABLE", resource.status());
        assertEquals(at, resource.redeemedAt());
        assertNull(resource.usedAt());
    }

    @Test
    @DisplayName("toResource(CartItem) should map the cart line (AAA)")
    void cartItem_MapsLine() {
        // Arrange
        var at = Instant.parse("2026-09-14T15:00:00Z");
        var item = new CartItem(1L, "cart-1", "ana", "kit-1", 2, at);

        // Act
        var resource = CommerceResourceAssembler.toResource(item);

        // Assert
        assertEquals("cart-1", resource.id());
        assertEquals("ana", resource.userId());
        assertEquals("kit-1", resource.productId());
        assertEquals(2, resource.quantity());
    }

    @Test
    @DisplayName("toResource(Order) should expose total, discounted total and coupon reference (AAA)")
    void order_MapsDiscountedTotals() {
        // Arrange
        var order = new Order(1L, "ord-1", "ana",
                List.of(new OrderItem("p1", "Bandage", new BigDecimal("100.00"), 2)), OrderStatus.PENDING,
                LocalDate.of(2026, 9, 14), null, PaymentStatus.NONE, null, null, null, 10, "rdc-1");

        // Act
        var resource = CommerceResourceAssembler.toResource(order);

        // Assert
        assertEquals(new BigDecimal("200.00"), resource.total());
        assertEquals(new BigDecimal("180.00"), resource.finalTotal());
        assertEquals(10, resource.appliedDiscountPercentage());
        assertEquals("rdc-1", resource.redeemedCouponExternalId());
        assertEquals("PENDING", resource.status());
        assertEquals(1, resource.items().size());
        assertEquals("NONE", resource.paymentStatus());
    }
}
