@checkout @US40 @US42
Feature: Use a redeemed coupon at checkout
  As a player who redeemed a discount coupon
  I want the discount applied when I create my order
  So that I pay the discounted price for the products in my cart

  Background:
    Given a signed-in player with 1000 SafeCoins

  Scenario: A redeemed coupon discounts the whole order
    Given the player has redeemed the coupon "cpn-10"
    And the cart contains 1 unit of the product "mochila-emergencia"
    When the player creates an order using the redeemed coupon
    Then the response status is 201
    And the order total is 159.90 and the final total is 143.91
    And the redeemed coupon is no longer available

  Scenario: A minimum purchase coupon is rejected below its minimum
    Given the player has redeemed the coupon "cpn-min-15"
    And the cart contains 1 unit of the product "mochila-emergencia"
    When the player creates an order using the redeemed coupon
    Then the response status is 422
    And the redeemed coupon is still available

  Scenario: A minimum purchase coupon is accepted once the minimum is reached
    Given the player has redeemed the coupon "cpn-min-15"
    And the cart contains 2 units of the product "mochila-emergencia"
    When the player creates an order using the redeemed coupon
    Then the response status is 201
    And the order total is 319.80 and the final total is 271.83

  Scenario: A coupon cannot be used twice
    Given the player has redeemed the coupon "cpn-5"
    And the cart contains 1 unit of the product "mochila-emergencia"
    And the player creates an order using the redeemed coupon
    And the cart contains 1 unit of the product "mochila-emergencia"
    When the player creates an order using the redeemed coupon
    Then the response status is 422

  Scenario: An order without a coupon charges the full price
    Given the cart contains 1 unit of the product "mascarilla-rcp"
    When the player creates an order without a coupon
    Then the response status is 201
    And the order total is 24.90 and the final total is 24.90

  Scenario: A player cannot create an order with an empty cart
    When the player creates an order without a coupon
    Then the response status is 422
