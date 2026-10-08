package com.safestep.platform.iam.interfaces.acl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.safestep.platform.iam.application.commandservices.UserCommandService;
import com.safestep.platform.iam.application.queryservices.UserQueryService;
import com.safestep.platform.iam.domain.model.aggregates.User;
import com.safestep.platform.iam.domain.model.commands.SignUpCommand;
import com.safestep.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.safestep.platform.iam.domain.model.queries.GetUserByUsernameQuery;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class IamContextFacadeTest {

    private UserCommandService commands;
    private UserQueryService queries;
    private IamContextFacade facade;

    @BeforeEach
    void setUp() {
        commands = mock(UserCommandService.class);
        queries = mock(UserQueryService.class);
        facade = new IamContextFacade(commands, queries);
    }

    private User userWithId(long id, String username) {
        var user = new User(username, "hash");
        user.setId(id);
        return user;
    }

    @Test
    @DisplayName("createUser(username, password) should sign up with the default role and return the id (AAA)")
    void createUser_UsesDefaultRole() {
        // Arrange
        when(commands.handle(any(SignUpCommand.class))).thenReturn(Result.success(userWithId(5, "ana")));

        // Act
        var id = facade.createUser("ana", "pwd");

        // Assert
        assertEquals(5L, id);
        var captor = ArgumentCaptor.forClass(SignUpCommand.class);
        verify(commands).handle(captor.capture());
        assertEquals(List.of("ROLE_USER"), captor.getValue().roles());
    }

    @Test
    @DisplayName("createUser should return 0 when the sign up fails (AAA)")
    void createUser_ReturnsZeroOnFailure() {
        // Arrange
        when(commands.handle(any(SignUpCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("User", "taken")));

        // Act
        var id = facade.createUser("ana", "pwd", List.of("ROLE_ADMIN"));

        // Assert
        assertEquals(0L, id);
    }

    @Test
    @DisplayName("createUser(username, password, roles) should treat null roles as an empty list (AAA)")
    void createUser_WithNullRoles_UsesEmptyList() {
        // Arrange
        when(commands.handle(any(SignUpCommand.class))).thenReturn(Result.success(userWithId(8, "ana")));

        // Act
        var id = facade.createUser("ana", "pwd", null);

        // Assert
        assertEquals(8L, id);
        var captor = ArgumentCaptor.forClass(SignUpCommand.class);
        verify(commands).handle(captor.capture());
        assertEquals(List.of(), captor.getValue().roles());
    }

    @Test
    @DisplayName("fetchUserIdByUsername should return the id or 0 when the user is missing (AAA)")
    void fetchUserIdByUsername_FoundAndMissing() {
        // Arrange
        when(queries.handle(new GetUserByUsernameQuery("ana"))).thenReturn(Optional.of(userWithId(5, "ana")));
        when(queries.handle(new GetUserByUsernameQuery("ghost"))).thenReturn(Optional.empty());

        // Act + Assert
        assertEquals(5L, facade.fetchUserIdByUsername("ana"));
        assertEquals(0L, facade.fetchUserIdByUsername("ghost"));
    }

    @Test
    @DisplayName("fetchUsernameByUserId should return the username or an empty string (AAA)")
    void fetchUsernameByUserId_FoundAndMissing() {
        // Arrange
        when(queries.handle(new GetUserByIdQuery(5L))).thenReturn(Optional.of(userWithId(5, "ana")));
        when(queries.handle(new GetUserByIdQuery(99L))).thenReturn(Optional.empty());

        // Act + Assert
        assertEquals("ana", facade.fetchUsernameByUserId(5L));
        assertEquals("", facade.fetchUsernameByUserId(99L));
    }
}
