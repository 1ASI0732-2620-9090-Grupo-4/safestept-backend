package com.safestep.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.safestep.acceptance.support.ScenarioContext;
import io.cucumber.java.en.Then;

public class CommonSteps {

    private final ScenarioContext context;

    public CommonSteps(ScenarioContext context) {
        this.context = context;
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        assertThat(context.response().status()).as("HTTP status of %s", context.response().body()).isEqualTo(status);
    }

    @Then("the response contains the error code {string}")
    public void theResponseContainsTheErrorCode(String code) {
        assertThat(context.response().text("code")).isEqualTo(code);
    }

    @Then("the response contains an access token")
    public void theResponseContainsAnAccessToken() {
        assertThat(context.response().text("token")).isNotBlank();
    }
}
