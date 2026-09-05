package com.safestep.platform.commerce.domain.model.valueobjects;

public record ProductRecommendation(Long id, String externalId, String username, String productId, String reason,
        String relatedSimulationId, String priority, boolean dismissed) {
}
