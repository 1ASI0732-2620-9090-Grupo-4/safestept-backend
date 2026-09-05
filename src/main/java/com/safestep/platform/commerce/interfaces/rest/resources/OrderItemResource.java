package com.safestep.platform.commerce.interfaces.rest.resources;

import java.math.BigDecimal;

public record OrderItemResource(String productId, String productName, BigDecimal unitPrice, int quantity) {
}
