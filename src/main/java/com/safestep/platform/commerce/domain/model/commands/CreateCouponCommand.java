package com.safestep.platform.commerce.domain.model.commands;

import com.safestep.platform.commerce.domain.model.aggregates.Coupon;

public record CreateCouponCommand(Coupon coupon) {
}
