package com.safestep.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.safestep.acceptance.support.ApiClient;
import com.safestep.acceptance.support.PlayerFactory;
import com.safestep.acceptance.support.ScenarioContext;
import com.safestep.platform.iam.application.commandservices.UserCommandService;
import com.safestep.platform.iam.domain.model.aggregates.User;
import com.safestep.platform.iam.domain.model.commands.SignUpCommand;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RoleManagementSteps {

    private final ApiClient api;
    private final PlayerFactory players;
    private final UserCommandService userCommandService;
    private final ScenarioContext context;
    private User regularPlayer;
    private String regularPlayerToken;

    public RoleManagementSteps(ApiClient api, PlayerFactory players, UserCommandService userCommandService,
            ScenarioContext context) {
        this.api = api;
        this.players = players;
        this.userCommandService = userCommandService;
        this.context = context;
    }

    @Given("an administrator is signed in")
    public void anAdministratorIsSignedIn() {
        var admin = createUser("admin", "ROLE_ADMIN");
        context.userId(admin.getId());
        players.signIn(context, admin.getUsername());
    }

    @Given("another administrator exists")
    public void anotherAdministratorExists() {
        createUser("backup-admin", "ROLE_ADMIN");
    }

    @Given("a regular player exists")
    public void aRegularPlayerExists() {
        regularPlayer = createUser("regular", "ROLE_USER");
        var signIn = api.post("/api/v1/authentication/sign-in",
                Map.of("username", regularPlayer.getUsername(), "password", ScenarioContext.PASSWORD), null);
        regularPlayerToken = signIn.text("token");
    }

    @When("the administrator assigns the roles {string} to the regular player")
    public void theAdministratorAssignsTheRolesToTheRegularPlayer(String roles) {
        context.response(api.put("/api/v1/users/%d/roles".formatted(regularPlayer.getId()),
                Map.of("roles", split(roles)), context.token()));
    }

    @When("the regular player tries to assign the roles {string} to themselves")
    public void theRegularPlayerTriesToAssignTheRolesToThemselves(String roles) {
        context.response(api.put("/api/v1/users/%d/roles".formatted(regularPlayer.getId()),
                Map.of("roles", split(roles)), regularPlayerToken));
    }

    @When("an anonymous visitor lists the users")
    public void anAnonymousVisitorListsTheUsers() {
        context.response(api.get("/api/v1/users", null));
    }

    @When("the administrator removes their own administrator role")
    public void theAdministratorRemovesTheirOwnAdministratorRole() {
        context.response(api.put("/api/v1/users/%d/roles".formatted(context.userId()),
                Map.of("roles", List.of("ROLE_USER")), context.token()));
    }

    @Then("the regular player has the roles {string}")
    public void theRegularPlayerHasTheRoles(String roles) {
        var response = api.get("/api/v1/users/%d".formatted(regularPlayer.getId()), context.token());
        var actual = new ArrayList<String>();
        response.body().get("roles").forEach(node -> actual.add(node.asText()));
        assertThat(actual).containsExactlyInAnyOrderElementsOf(split(roles));
    }

    private User createUser(String prefix, String role) {
        var username = ScenarioContext.uniqueUsername(prefix);
        return userCommandService.handle(new SignUpCommand(username, ScenarioContext.PASSWORD, List.of(role)))
                .toOptional().orElseThrow();
    }

    private List<String> split(String roles) {
        return Arrays.stream(roles.split(",")).map(String::trim).toList();
    }
}
