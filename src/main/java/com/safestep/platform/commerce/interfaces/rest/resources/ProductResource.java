package com.safestep.platform.commerce.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

public record ProductResource(String id, String name, String category, String type, BigDecimal price,
        BigDecimal oldPrice, double rating, int stock, String imageUrl, List<String> tags, String description) {
}
