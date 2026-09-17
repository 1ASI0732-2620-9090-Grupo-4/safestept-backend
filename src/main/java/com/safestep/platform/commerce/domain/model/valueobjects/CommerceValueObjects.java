package com.safestep.platform.commerce.domain.model.valueobjects;

import java.math.*;

public final class CommerceValueObjects {
    private CommerceValueObjects() {
    }

    public record Money(BigDecimal value) {
        public Money {
            if (value == null || value.signum() < 0)
                throw new IllegalArgumentException("Money cannot be negative");
            value = value.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public record Stock(int value) {
        public Stock {
            if (value < 0)
                throw new IllegalArgumentException("Stock cannot be negative");
        }
    }

    public enum OrderStatus {
        PENDING, PAYMENT_PENDING, PAID, SHIPPED, DELIVERED, CANCELLED, PAYMENT_FAILED;

        public static OrderStatus from(String v) {
            if (v == null)
                return PENDING;
            var n = v.toUpperCase();
            if (n.contains("PAYMENT_PENDING"))
                return PAYMENT_PENDING;
            if (n.contains("COMPR") || n.contains("PAID"))
                return PAID;
            if (n.contains("ENVI") || n.contains("SHIP"))
                return SHIPPED;
            if (n.contains("ENTREG") || n.contains("DELIVER"))
                return DELIVERED;
            if (n.contains("CANCEL"))
                return CANCELLED;
            if (n.contains("PAYMENT_FAILED") || n.contains("FAILED"))
                return PAYMENT_FAILED;
            return PENDING;
        }
    }

    public enum PaymentStatus {
        NONE, PENDING, PAID, FAILED, CANCELLED;

        public static PaymentStatus from(String v) {
            if (v == null || v.isBlank())
                return NONE;
            try {
                return valueOf(v.toUpperCase());
            } catch (Exception e) {
                return NONE;
            }
        }
    }

    public enum ProductType {
        PRODUCT, KIT, SERVICE;

        public static ProductType from(String v) {
            if (v == null)
                return PRODUCT;
            try {
                return valueOf(v.toUpperCase());
            } catch (Exception e) {
                return PRODUCT;
            }
        }
    }

    public enum CouponType {
        PERCENTAGE_OFF, PERCENTAGE_OFF_MIN_PURCHASE;

        public static CouponType from(String v) {
            if (v == null)
                return PERCENTAGE_OFF;
            try {
                return valueOf(v.toUpperCase());
            } catch (Exception e) {
                return PERCENTAGE_OFF;
            }
        }
    }

    public enum RedemptionStatus {
        AVAILABLE, USED;

        public static RedemptionStatus from(String v) {
            if (v == null)
                return AVAILABLE;
            try {
                return valueOf(v.toUpperCase());
            } catch (Exception e) {
                return AVAILABLE;
            }
        }
    }
}
