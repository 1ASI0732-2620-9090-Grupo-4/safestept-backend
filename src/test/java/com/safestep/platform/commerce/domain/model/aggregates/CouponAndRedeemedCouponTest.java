package com.safestep.platform.commerce.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.RedemptionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CouponAndRedeemedCouponTest {

    private RedeemedCoupon availableCoupon() {
        return new RedeemedCoupon(1L, "rdc-1", "ana", "cpn-5", "5% off", CouponType.PERCENTAGE_OFF, 5, null,
                Instant.parse("2026-09-14T15:00:00Z"), null, RedemptionStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Coupon should default to a simple percentage type when none is given (AAA)")
    void coupon_DefaultsToSimplePercentage() {
        // Act
        var coupon = new Coupon(1L, "cpn-5", "5% off", 150, null, 5, new BigDecimal("100.00"));

        // Assert
        assertEquals(CouponType.PERCENTAGE_OFF, coupon.getType());
        assertNull(coupon.getMinPurchaseAmount(), "A simple coupon never carries a minimum purchase");
    }

    @Test
    @DisplayName("Coupon should keep the minimum purchase only for the minimum-purchase type (AAA)")
    void coupon_KeepsMinimumPurchaseForMinPurchaseType() {
        // Act
        var coupon = new Coupon(2L, "cpn-min", "10% over S/150", 300, CouponType.PERCENTAGE_OFF_MIN_PURCHASE, 10,
                new BigDecimal("150.00"));

        // Assert
        assertEquals(new BigDecimal("150.00"), coupon.getMinPurchaseAmount());
        assertEquals(10, coupon.getDiscountPercentage());
        assertEquals(300, coupon.getCostCoins());
        assertEquals("cpn-min", coupon.getExternalId());
    }

    @Test
    @DisplayName("RedeemedCoupon should start available and become used once (AAA)")
    void redeemedCoupon_IsUsedOnlyOnce() {
        // Arrange
        var coupon = availableCoupon();
        var usedAt = Instant.parse("2026-09-15T10:00:00Z");

        // Act
        coupon.markUsed(usedAt);

        // Assert
        assertEquals(RedemptionStatus.USED, coupon.getStatus());
        assertEquals(usedAt, coupon.getUsedAt());
        assertThrows(IllegalStateException.class, () -> coupon.markUsed(Instant.now()));
    }

    @Test
    @DisplayName("RedeemedCoupon should become available again when released (AAA)")
    void redeemedCoupon_ReleaseMakesItAvailableAgain() {
        // Arrange
        var coupon = availableCoupon();
        coupon.markUsed(Instant.now());

        // Act
        coupon.release();

        // Assert
        assertEquals(RedemptionStatus.AVAILABLE, coupon.getStatus());
        assertNull(coupon.getUsedAt());
        coupon.markUsed(Instant.now());
        assertNotNull(coupon.getUsedAt());
    }

    @Test
    @DisplayName("RedeemedCoupon should default the type and status when they are missing (AAA)")
    void redeemedCoupon_DefaultsTypeAndStatus() {
        // Act
        var coupon = new RedeemedCoupon(null, "rdc-2", "ana", "cpn", "T", null, 10, null, Instant.now(), null, null);

        // Assert
        assertEquals(CouponType.PERCENTAGE_OFF, coupon.getType());
        assertEquals(RedemptionStatus.AVAILABLE, coupon.getStatus());
    }
}
