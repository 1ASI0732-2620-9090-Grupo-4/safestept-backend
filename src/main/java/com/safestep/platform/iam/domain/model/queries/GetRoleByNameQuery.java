package com.safestep.platform.iam.domain.model.queries;

import com.safestep.platform.iam.domain.model.valueobjects.Roles;

/**
 * Get role by name query
 * <p>
 * This class represents the query to get a role by its name.
 * </p>
 *
 * @param name
 *            the name of the role
 *
 * @see com.safestep.platform.iam.domain.model.valueobjects.Roles
 */
public record GetRoleByNameQuery(Roles name) {
}
