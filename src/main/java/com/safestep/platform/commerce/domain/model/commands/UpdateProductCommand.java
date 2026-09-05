package com.safestep.platform.commerce.domain.model.commands;

import com.safestep.platform.commerce.domain.model.aggregates.Product;

public record UpdateProductCommand(String productId, Product product) {
}
