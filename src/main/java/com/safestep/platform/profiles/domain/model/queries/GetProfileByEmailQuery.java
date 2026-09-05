package com.safestep.platform.profiles.domain.model.queries;

import com.safestep.platform.profiles.domain.model.valueobjects.EmailAddress;

/**
 * Get Profile By Email Query
 */
public record GetProfileByEmailQuery(EmailAddress emailAddress) {
}
