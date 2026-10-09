@coupons @US42 @US59 @US61
Feature: Redeem SafeCoins for store coupons
  As a player who earns SafeCoins by training
  I want to exchange my SafeCoins for discount coupons
  So that I pay less when I buy emergency products

  Background:
    Given a signed-in player with 500 SafeCoins

  Scenario: A player redeems a coupon they can afford
    When the player redeems the coupon "cpn-5"
    Then the response status is 201
    And the player has 350 SafeCoins left
    And the coupon "cpn-5" is listed as available in the player's coupons

  Scenario: A player cannot redeem a coupon that costs more than their balance
    When the player redeems the coupon "cpn-15"
    Then the response status is 422
    And the response contains the error code "BUSINESS_RULE_VIOLATION"
    And the player has 500 SafeCoins left
    And the player has no redeemed coupons

  Scenario: A player cannot redeem a coupon that does not exist
    When the player redeems the coupon "cpn-ghost"
    Then the response status is 404

  Scenario: An anonymous visitor cannot redeem coupons
    When an anonymous visitor redeems the coupon "cpn-5"
    Then the response status is 401

  Scenario Outline: Every catalogue coupon is charged at its published price
    When the player redeems the coupon "<coupon>"
    Then the response status is 201
    And the player has <remaining> SafeCoins left
    And the redeemed coupon gives <discount> percent off

    Examples:
      | coupon     | remaining | discount |
      | cpn-5      | 350       | 5        |
      | cpn-10     | 150       | 10       |
      | cpn-min-5  | 380       | 5        |
      | cpn-min-10 | 200       | 10       |
