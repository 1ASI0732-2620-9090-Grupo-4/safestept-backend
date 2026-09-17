package com.safestep.platform.commerce.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;

public record RedeemedCouponResource(String id, String couponId, String title, String type, int discountPercentage,
        BigDecimal minPurchaseAmount, Instant redeemedAt, Instant usedAt, String status) {
}
