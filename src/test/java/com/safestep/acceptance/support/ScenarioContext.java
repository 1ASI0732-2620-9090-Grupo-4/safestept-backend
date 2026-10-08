package com.safestep.acceptance.support;

import java.util.UUID;

/** State shared by the step definitions of a single Cucumber scenario. */
public class ScenarioContext {

    public static final String PASSWORD = "SecurePass123!";

    private String username;
    private String token;
    private Long userId;
    private ApiResponse response;
    private String redeemedCouponId;

    public static String uniqueUsername(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String username() {
        return username;
    }

    public void username(String username) {
        this.username = username;
    }

    public String token() {
        return token;
    }

    public void token(String token) {
        this.token = token;
    }

    public Long userId() {
        return userId;
    }

    public void userId(Long userId) {
        this.userId = userId;
    }

    public ApiResponse response() {
        return response;
    }

    public void response(ApiResponse response) {
        this.response = response;
    }

    public String redeemedCouponId() {
        return redeemedCouponId;
    }

    public void redeemedCouponId(String redeemedCouponId) {
        this.redeemedCouponId = redeemedCouponId;
    }
}
