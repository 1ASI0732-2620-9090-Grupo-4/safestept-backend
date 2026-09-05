package com.safestep.platform.iam.domain.model.aggregates;

import com.safestep.platform.iam.domain.model.entities.Role;
import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User aggregate root.
 */
@Getter
public class User extends AbstractDomainAggregateRoot<User> {

    @Setter
    private Long id;
    @Setter
    private String username;
    @Setter
    private String password;
    @Setter
    private Set<Role> roles;
    @Setter
    private boolean enabled = true;
    @Setter
    private boolean accountNonLocked = true;
    @Setter
    private boolean accountNonExpired = true;
    @Setter
    private boolean credentialsNonExpired = true;

    public User() {
        this.roles = new HashSet<>();
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
    }

    public User(String username, String password, List<Role> roles) {
        this(username, password);
        addRoles(roles);
    }

    /**
     * Add a role to the user.
     *
     * @param role
     *            the role to add
     *
     * @return the user with the added role
     */
    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    /**
     * Add a list of roles to the user.
     *
     * @param roles
     *            the list of roles to add
     *
     * @return the user with the added roles
     */
    public User addRoles(List<Role> roles) {
        var validatedRoleSet = Role.validateRoleSet(roles);
        this.roles.addAll(validatedRoleSet);
        return this;
    }

    public User updateStatus(boolean enabled, boolean accountNonLocked, boolean accountNonExpired,
            boolean credentialsNonExpired) {
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        return this;
    }
}
