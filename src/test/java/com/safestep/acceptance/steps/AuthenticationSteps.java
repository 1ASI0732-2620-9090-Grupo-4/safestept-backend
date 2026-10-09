package com.safestep.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.safestep.acceptance.support.ApiClient;
import com.safestep.acceptance.support.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthenticationSteps {

    private final ApiClient api;
    private final ScenarioContext context;

    public AuthenticationSteps(ApiClient api, ScenarioContext context) {
        this.api = api;
        this.context = context;
    }

    @When("a visitor registers with a new username and a valid password")
    public void aVisitorRegistersWithANewUsernameAndAValidPassword() {
        context.response(api.post("/api/v1/authentication/sign-up",
                Map.of("username", ScenarioContext.uniqueUsername("visitor"), "password", ScenarioContext.PASSWORD),
                null));
    }

    @When("a visitor registers requesting the role {string}")
    public void aVisitorRegistersRequestingTheRole(String role) {
        context.response(api.post("/api/v1/authentication/sign-up", Map.of("username",
                ScenarioContext.uniqueUsername("sneaky"), "password", ScenarioContext.PASSWORD, "roles", List.of(role)),
                null));
    }

    @When("a visitor registers with the username {string} and the password {string}")
    public void aVisitorRegistersWithTheUsernameAndThePassword(String username, String password) {
        context.response(api.post("/api/v1/authentication/sign-up",
                Map.of("username", username, "password", password), null));
    }

    @Given("a registered user")
    public void aRegisteredUser() {
        var username = ScenarioContext.uniqueUsername("player");
        var response = api.post("/api/v1/authentication/sign-up",
                Map.of("username", username, "password", ScenarioContext.PASSWORD), null);
        assertThat(response.status()).isEqualTo(201);
        context.username(username);
        context.userId(response.body().get("id").asLong());
    }

    @When("a visitor registers with the username of the registered user")
    public void aVisitorRegistersWithTheUsernameOfTheRegisteredUser() {
        context.response(api.post("/api/v1/authentication/sign-up",
                Map.of("username", context.username(), "password", ScenarioContext.PASSWORD), null));
    }

    @When("the user signs in with the right password")
    public void theUserSignsInWithTheRightPassword() {
        theUserSignsInWithThePassword(ScenarioContext.PASSWORD);
    }

    @When("the user signs in with the password {string}")
    public void theUserSignsInWithThePassword(String password) {
        context.response(api.post("/api/v1/authentication/sign-in",
                Map.of("username", context.username(), "password", password), null));
    }

    @Then("the new account only has the role {string}")
    public void theNewAccountOnlyHasTheRole(String role) {
        var roles = new ArrayList<String>();
        context.response().body().get("roles").forEach(node -> roles.add(node.asText()));
        assertThat(roles).containsExactly(role);
    }
}
