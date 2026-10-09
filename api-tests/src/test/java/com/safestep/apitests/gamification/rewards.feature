@gamification @US15 @US16
Feature: Simulation rewards API

  Background:
    * url karate.properties['api.baseUrl']
    * def player = call read('classpath:com/safestep/apitests/helpers/register-and-sign-in.feature')

  Scenario: A new player starts without SafeCoins
    Given path 'api/v1/gamification/summary/me'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match response == { username: '#(player.username)', level: 1, xp: 0, safeCoins: 0, streak: 0, completedSimulations: 0 }

  Scenario: Completing a simulation rewards coins and XP and is recorded in the coin history
    * call read('classpath:com/safestep/apitests/helpers/complete-simulation.feature') { token: '#(player.token)', slug: 'rcp-basico', score: 90 }
    Given path 'api/v1/gamification/summary/me'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match response.safeCoins == 101
    And match response.xp == 420
    And match response.completedSimulations == 1
    Given path 'api/v1/gamification/coin-transactions/me'
    And header Authorization = 'Bearer ' + player.token
    When method get
    Then status 200
    And match response == '#[1]'
    And match response[0].earnedCoins == 101

  Scenario Outline: An attempt with an out of range score is rejected - <score>
    Given path 'api/v1/simulations/rcp-basico/attempts'
    And header Authorization = 'Bearer ' + player.token
    And request { mode: 'practice', startedAt: '2026-09-14T15:00:00Z', score: <score>, totalSteps: 5, correctSteps: 4, timeElapsed: 100 }
    When method post
    Then status 400

    Examples:
      | score |
      | -1    |
      | 101   |

  Scenario: A simulation that does not exist cannot be attempted
    Given path 'api/v1/simulations/ghost/attempts'
    And header Authorization = 'Bearer ' + player.token
    And request { mode: 'practice', startedAt: '2026-09-14T15:00:00Z', score: 80, totalSteps: 5, correctSteps: 4, timeElapsed: 100 }
    When method post
    Then status 404
