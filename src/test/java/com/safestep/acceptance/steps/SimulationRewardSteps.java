package com.safestep.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.safestep.acceptance.support.ApiClient;
import com.safestep.acceptance.support.ScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class SimulationRewardSteps {

    private final ApiClient api;
    private final ScenarioContext context;

    public SimulationRewardSteps(ApiClient api, ScenarioContext context) {
        this.api = api;
        this.context = context;
    }

    @When("the player completes the simulation {string} with a score of {int}")
    public void thePlayerCompletesTheSimulationWithAScoreOf(String simulationId, int score) {
        var startedAt = Instant.now().minusSeconds(300);
        var payload = Map.of("mode", "evaluation", "startedAt", startedAt.toString(), "completedAt",
                Instant.now().toString(), "score", score, "totalSteps", 5, "correctSteps", 4, "timeElapsed", 300,
                "errors", List.of());
        context.response(api.post("/api/v1/simulations/%s/attempts".formatted(simulationId), payload, context.token()));
    }

    @Then("the player's summary shows {int} SafeCoins and {int} XP")
    public void thePlayersSummaryShowsSafeCoinsAndXp(int coins, int xp) {
        var summary = api.get("/api/v1/gamification/summary/me", context.token()).body();
        assertThat(summary.get("safeCoins").asInt()).isEqualTo(coins);
        assertThat(summary.get("xp").asInt()).isEqualTo(xp);
    }

    @Then("the player's summary shows {int} completed simulation(s)")
    public void thePlayersSummaryShowsCompletedSimulations(int completed) {
        var summary = api.get("/api/v1/gamification/summary/me", context.token()).body();
        assertThat(summary.get("completedSimulations").asInt()).isEqualTo(completed);
    }
}
