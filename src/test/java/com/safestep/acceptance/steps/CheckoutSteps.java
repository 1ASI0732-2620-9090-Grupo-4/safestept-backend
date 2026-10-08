package com.safestep.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.safestep.acceptance.support.ApiClient;
import com.safestep.acceptance.support.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CheckoutSteps {

    private final ApiClient api;
    private final ScenarioContext context;

    public CheckoutSteps(ApiClient api, ScenarioContext context) {
        this.api = api;
        this.context = context;
    }

    @Given("the cart contains {int} unit(s) of the product {string}")
    public void theCartContainsUnitsOfTheProduct(int quantity, String productId) {
        var response = api.post("/api/v1/commerce/cart/items", Map.of("productId", productId, "quantity", quantity),
                context.token());
        assertThat(response.status()).isEqualTo(201);
    }

    @When("the player creates an order using the redeemed coupon")
    public void thePlayerCreatesAnOrderUsingTheRedeemedCoupon() {
        createOrder(context.redeemedCouponId());
    }

    @When("the player creates an order without a coupon")
    public void thePlayerCreatesAnOrderWithoutACoupon() {
        createOrder(null);
    }

    @Then("the order total is {bigdecimal} and the final total is {bigdecimal}")
    public void theOrderTotalIsAndTheFinalTotalIs(BigDecimal total, BigDecimal finalTotal) {
        var body = context.response().body();
        assertThat(body.get("total").decimalValue()).isEqualByComparingTo(total);
        assertThat(body.get("finalTotal").decimalValue()).isEqualByComparingTo(finalTotal);
    }

    @Then("the redeemed coupon is no longer available")
    public void theRedeemedCouponIsNoLongerAvailable() {
        assertThat(statusOfRedeemedCoupon()).isEqualTo("USED");
    }

    @Then("the redeemed coupon is still available")
    public void theRedeemedCouponIsStillAvailable() {
        assertThat(statusOfRedeemedCoupon()).isEqualTo("AVAILABLE");
    }

    private void createOrder(String redeemedCouponId) {
        var payload = new HashMap<String, Object>();
        payload.put("status", "PENDING");
        payload.put("redeemedCouponExternalId", redeemedCouponId);
        context.response(api.post("/api/v1/commerce/orders", payload, context.token()));
    }

    private String statusOfRedeemedCoupon() {
        var coupons = api.get("/api/v1/commerce/coupons/redeemed/me", context.token()).body();
        for (var coupon : coupons) {
            if (context.redeemedCouponId().equals(coupon.get("id").asText())) {
                return coupon.get("status").asText();
            }
        }
        throw new AssertionError("Redeemed coupon %s was not found".formatted(context.redeemedCouponId()));
    }
}
