package com.safestep.platform.commerce.interfaces.rest.resources;

import java.math.BigDecimal;

public record CouponResource(String id, String title, int costCoins, String type, int discountPercentage,
        BigDecimal minPurchaseAmount) {
}
