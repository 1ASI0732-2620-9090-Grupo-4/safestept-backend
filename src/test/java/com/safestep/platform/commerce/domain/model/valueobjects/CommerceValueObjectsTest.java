package com.safestep.platform.commerce.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.Money;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.OrderStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.PaymentStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.ProductType;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.RedemptionStatus;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.Stock;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommerceValueObjectsTest {

    @Test
    @DisplayName("Money should round to two decimals and reject negative amounts (AAA)")
    void money_RoundsAndRejectsNegatives() {
        // Assert
        assertEquals(new BigDecimal("10.57"), new Money(new BigDecimal("10.567")).value());
        assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("-0.01")));
        assertThrows(IllegalArgumentException.class, () -> new Money(null));
    }

    @Test
    @DisplayName("Stock should reject negative quantities (AAA)")
    void stock_RejectsNegativeValues() {
        // Assert
        assertEquals(0, new Stock(0).value());
        assertThrows(IllegalArgumentException.class, () -> new Stock(-1));
    }

    @Test
    @DisplayName("OrderStatus.from should understand English and Spanish labels (AAA)")
    void orderStatus_FromLabels() {
        // Assert
        assertEquals(OrderStatus.PENDING, OrderStatus.from(null));
        assertEquals(OrderStatus.PAYMENT_PENDING, OrderStatus.from("payment_pending"));
        assertEquals(OrderStatus.PAID, OrderStatus.from("Comprado"));
        assertEquals(OrderStatus.PAID, OrderStatus.from("paid"));
        assertEquals(OrderStatus.SHIPPED, OrderStatus.from("Enviado"));
        assertEquals(OrderStatus.DELIVERED, OrderStatus.from("Entregado"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.from("Cancelado"));
        assertEquals(OrderStatus.PAYMENT_FAILED, OrderStatus.from("payment_failed"));
        assertEquals(OrderStatus.PENDING, OrderStatus.from("anything else"));
    }

    @Test
    @DisplayName("lenient enums should fall back to a safe default for unknown values (AAA)")
    void lenientEnums_FallBackToDefaults() {
        // Assert
        assertEquals(PaymentStatus.NONE, PaymentStatus.from(null));
        assertEquals(PaymentStatus.NONE, PaymentStatus.from(" "));
        assertEquals(PaymentStatus.PAID, PaymentStatus.from("paid"));
        assertEquals(PaymentStatus.NONE, PaymentStatus.from("weird"));
        assertEquals(ProductType.PRODUCT, ProductType.from(null));
        assertEquals(ProductType.KIT, ProductType.from("kit"));
        assertEquals(ProductType.PRODUCT, ProductType.from("weird"));
        assertEquals(CouponType.PERCENTAGE_OFF, CouponType.from(null));
        assertEquals(CouponType.PERCENTAGE_OFF_MIN_PURCHASE, CouponType.from("percentage_off_min_purchase"));
        assertEquals(CouponType.PERCENTAGE_OFF, CouponType.from("weird"));
        assertEquals(RedemptionStatus.AVAILABLE, RedemptionStatus.from(null));
        assertEquals(RedemptionStatus.USED, RedemptionStatus.from("used"));
        assertEquals(RedemptionStatus.AVAILABLE, RedemptionStatus.from("weird"));
    }
}
