package com.safestep.platform.commerce.domain.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartAndOrderItemTest {

    @Test
    @DisplayName("CartItem should reject quantities below one at creation and on change (AAA)")
    void cartItem_RejectsInvalidQuantities() {
        // Arrange
        var item = new CartItem(1L, "cart-1", "ana", "p1", 2, Instant.now());

        // Act
        item.changeQuantity(5);

        // Assert
        assertEquals(5, item.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> item.changeQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> new CartItem(2L, "cart-2", "ana", "p1", 0, Instant.now()));
        assertEquals(5, item.getQuantity());
    }

    @Test
    @DisplayName("OrderItem should compute its subtotal and reject empty quantities (AAA)")
    void orderItem_ComputesSubtotal() {
        // Act
        var item = new OrderItem("p1", "Bandage", new BigDecimal("12.50"), 3);

        // Assert
        assertEquals(new BigDecimal("37.50"), item.subtotal());
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("p1", "Bandage", BigDecimal.TEN, 0));
    }
}
