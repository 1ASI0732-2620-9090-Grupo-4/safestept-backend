package com.safestep.acceptance.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.safestep.platform.gamification.domain.model.aggregates.PlayerProgress;
import com.safestep.platform.gamification.domain.repositories.PlayerProgressRepository;
import java.util.Map;

/** Creates the players and balances the scenarios need before they start exercising the API. */
public class PlayerFactory {

    private final ApiClient api;
    private final PlayerProgressRepository progress;

    public PlayerFactory(ApiClient api, PlayerProgressRepository progress) {
        this.api = api;
        this.progress = progress;
    }

    public void registerAndSignIn(ScenarioContext context, String prefix) {
        var username = ScenarioContext.uniqueUsername(prefix);
        var signUp = api.post("/api/v1/authentication/sign-up",
                Map.of("username", username, "password", ScenarioContext.PASSWORD), null);
        assertThat(signUp.status()).isEqualTo(201);
        context.userId(signUp.body().get("id").asLong());
        signIn(context, username);
    }

    public void signIn(ScenarioContext context, String username) {
        var signIn = api.post("/api/v1/authentication/sign-in",
                Map.of("username", username, "password", ScenarioContext.PASSWORD), null);
        assertThat(signIn.status()).isEqualTo(200);
        context.username(username);
        context.token(signIn.text("token"));
    }

    public void grantCoins(String username, int coins) {
        progress.save(new PlayerProgress(null, username, 1, 0, coins, 0, 0, null));
    }
}
