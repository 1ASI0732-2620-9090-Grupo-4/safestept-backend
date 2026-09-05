package com.safestep.platform.profiles.infrastructure.persistence.jpa.converters;

public record EmailAddressValue(String address) {
    public EmailAddressValue {
        if (address == null || address.isBlank())
            throw new IllegalArgumentException("address must not be null or blank");
    }
}
