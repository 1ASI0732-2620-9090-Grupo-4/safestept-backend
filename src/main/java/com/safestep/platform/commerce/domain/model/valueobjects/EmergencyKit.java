package com.safestep.platform.commerce.domain.model.valueobjects;

import java.math.*;

public record EmergencyKit(Long id, String externalId, String name, String description, String level,
        BigDecimal individualPrice, BigDecimal kitPrice, BigDecimal savings, int savingsPercentage, String imageUrl,
        boolean popular) {
}
