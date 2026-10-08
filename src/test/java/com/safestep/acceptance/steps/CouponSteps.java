package com.safestep.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.safestep.acceptance.support.ApiClient;
import com.safestep.acceptance.support.PlayerFactory;
import com.safestep.acceptance.support.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;

public class CouponSteps {

    private final ApiClient api;
    private final PlayerFactory players;
    private final ScenarioContext context;

    public CouponSteps(ApiClient api, PlayerFactory players, ScenarioContext context) {
        this.api = api;
        this.players = players;
        this.context = context;
    }

    @Given("a signed-in player with {int} SafeCoins")
    public void aSignedInPlayerWithSafeCoins(int coins) {
        players.registerAndSignIn(context, "player");
        players.grantCoins(context.username(), coins);
    }

    @Given("a signed-in player with no SafeCoins")
    public void aSignedInPlayerWithNoSafeCoins() {
        players.registerAndSignIn(context, "newbie");
    }

    @Given("the player has redeemed the coupon {string}")
    public void thePlayerHasRedeemedTheCoupon(String couponId) {
        thePlayerRedeemsTheCoupon(couponId);
        assertThat(context.response().status()).isEqualTo(201);
    }

    @When("the player redeems the coupon {string}")
    public void thePlayerRedeemsTheCoupon(String couponId) {
        context.response(api.post("/api/v1/commerce/coupons/%s/redeem".formatted(couponId), Map.of(), context.token()));
        if (context.response().status() == 201) {
            context.redeemedCouponId(context.response().text("id"));
        }
    }

    @When("an anonymous visitor redeems the coupon {string}")
    public void anAnonymousVisitorRedeemsTheCoupon(String couponId) {
        context.response(api.post("/api/v1/commerce/coupons/%s/redeem".formatted(couponId), Map.of(), null));
    }

    @Then("the player has {int} SafeCoins left")
    public void thePlayerHasSafeCoinsLeft(int coins) {
        var summary = api.get("/api/v1/gamification/summary/me", context.token());
        assertThat(summary.status()).isEqualTo(200);
        assertThat(summary.body().get("safeCoins").asInt()).isEqualTo(coins);
    }

    @Then("the coupon {string} is listed as available in the player's coupons")
    public void theCouponIsListedAsAvailable(String couponId) {
        var coupons = api.get("/api/v1/commerce/coupons/redeemed/me", context.token()).body();
        var found = false;
        for (var coupon : coupons) {
            if (couponId.equals(coupon.get("couponId").asText())) {
                found = true;
                assertThat(coupon.get("status").asText()).isEqualTo("AVAILABLE");
            }
        }
        assertThat(found).as("coupon %s should be listed", couponId).isTrue();
    }

    @Then("the player has no redeemed coupons")
    public void thePlayerHasNoRedeemedCoupons() {
        var coupons = api.get("/api/v1/commerce/coupons/redeemed/me", context.token()).body();
        assertThat(coupons.size()).isZero();
    }

    @Then("the redeemed coupon gives {int} percent off")
    public void theRedeemedCouponGivesPercentOff(int percent) {
        assertThat(context.response().body().get("discountPercentage").asInt()).isEqualTo(percent);
    }
}
