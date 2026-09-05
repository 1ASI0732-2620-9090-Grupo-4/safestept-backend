package com.safestep.platform.commerce.interfaces.rest.resources;

import java.time.Instant;

public record CartItemResource(String id, String userId, String productId, int quantity, Instant addedAt) {
}
