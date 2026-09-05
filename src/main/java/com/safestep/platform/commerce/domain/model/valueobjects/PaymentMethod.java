package com.safestep.platform.commerce.domain.model.valueobjects;

import java.math.*;

public record PaymentMethod(Long id, String externalId, String type, String label, String description,
        BigDecimal processingFee, boolean available) {
}
