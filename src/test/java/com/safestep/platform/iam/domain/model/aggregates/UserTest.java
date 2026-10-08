package com.safestep.platform.iam.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.safestep.platform.iam.domain.model.entities.Role;
import com.safestep.platform.iam.domain.model.valueobjects.Roles;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("a new user should start enabled with every account flag open (AAA)")
    void newUser_StartsWithOpenAccountFlags() {
        // Act
        var user = new User("ana", "hash");

        // Assert
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    @DisplayName("replaceRoles should swap the whole role set (AAA)")
    void replaceRoles_SwapsTheWholeSet() {
        // Arrange
        var user = new User("ana", "hash", List.of(new Role(Roles.ROLE_USER)));

        // Act
        user.replaceRoles(List.of(new Role(Roles.ROLE_ADMIN), new Role(Roles.ROLE_INSTRUCTOR)));

        // Assert
        assertEquals(2, user.getRoles().size());
        assertFalse(user.getRoles().stream().anyMatch(role -> role.getStringName().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("replaceRoles should fall back to the default role when given none (AAA)")
    void replaceRoles_FallsBackToDefaultRole() {
        // Arrange
        var user = new User("ana", "hash", List.of(new Role(Roles.ROLE_ADMIN)));

        // Act
        user.replaceRoles(List.of());

        // Assert
        assertEquals(1, user.getRoles().size());
        assertEquals("ROLE_USER", user.getRoles().iterator().next().getStringName());
    }

    @Test
    @DisplayName("updateStatus should set the four account flags (AAA)")
    void updateStatus_SetsAllFlags() {
        // Arrange
        var user = new User("ana", "hash");

        // Act
        user.updateStatus(false, false, true, false);

        // Assert
        assertFalse(user.isEnabled());
        assertFalse(user.isAccountNonLocked());
        assertTrue(user.isAccountNonExpired());
        assertFalse(user.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("addRole and addRoles should accumulate roles without duplicates (AAA)")
    void addRoles_AccumulatesWithoutDuplicates() {
        // Arrange
        var user = new User("ana", "hash");

        // Act
        user.addRole(new Role(Roles.ROLE_USER));
        user.addRoles(List.of(new Role(Roles.ROLE_USER), new Role(Roles.ROLE_ADMIN)));

        // Assert
        assertEquals(2, user.getRoles().size());
    }

    @Test
    @DisplayName("Role.toRoleFromName should reject names that are not a known role (AAA)")
    void role_RejectsUnknownNames() {
        // Assert
        assertEquals(Roles.ROLE_ADMIN, Role.toRoleFromName("ROLE_ADMIN").getName());
        assertThrows(IllegalArgumentException.class, () -> Role.toRoleFromName("ROLE_ROOT"));
    }
}
