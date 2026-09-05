package com.safestep.platform.iam.domain.repositories;

import com.safestep.platform.iam.domain.model.entities.Role;
import com.safestep.platform.iam.domain.model.valueobjects.Roles;

import java.util.List;
import java.util.Optional;

/**
 * IAM role repository port.
 */
public interface RoleRepository {
    Optional<Role> findByName(Roles name);

    List<Role> findAll();

    Role save(Role role);

    boolean existsByName(Roles name);
}
