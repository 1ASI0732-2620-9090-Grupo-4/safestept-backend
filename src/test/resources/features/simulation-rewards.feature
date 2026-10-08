@gamification @simulation @US15 @US16
Feature: Earn SafeCoins and XP by completing simulations
  As a player training first aid
  I want to be rewarded when I complete a medical simulation
  So that I stay motivated and can later exchange coins for coupons

  Scenario: Completing a simulation rewards the player with coins and XP
    Given a signed-in player with no SafeCoins
    When the player completes the simulation "rcp-basico" with a score of 90
    Then the response status is 201
    And the player's summary shows 101 SafeCoins and 420 XP

  Scenario: Completing a simulation counts it in the player's summary
    Given a signed-in player with no SafeCoins
    When the player completes the simulation "rcp-basico" with a score of 80
    Then the player's summary shows 1 completed simulation

  Scenario: A player cannot complete a simulation that does not exist
    Given a signed-in player with no SafeCoins
    When the player completes the simulation "ghost-simulation" with a score of 90
    Then the response status is 404

  Scenario Outline: An attempt with an out of range score is rejected
    Given a signed-in player with no SafeCoins
    When the player completes the simulation "rcp-basico" with a score of <score>
    Then the response status is 400

    Examples:
      | score |
      | -1    |
      | 101   |
