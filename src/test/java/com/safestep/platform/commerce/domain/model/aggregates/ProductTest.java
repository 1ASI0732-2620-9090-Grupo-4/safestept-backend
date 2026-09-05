package com.safestep.platform.commerce.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import java.math.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    @Test
    void preventsSellingMoreThanAvailableStock() {
        var product = new Product(null, "mask", "Mask", "Protection", "Product", BigDecimal.TEN, null, 4.5, 2, "", "",
                List.of());
        assertThrows(IllegalArgumentException.class, () -> product.reduceStock(3));
        assertEquals(2, product.getStock());
    }
}
