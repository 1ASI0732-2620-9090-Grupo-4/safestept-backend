@authentication @US01 @US02
Feature: Authentication API (/api/v1/authentication)

  Background:
    * url karate.properties['api.baseUrl']

  # Data-driven: every record of users-batch.json becomes one scenario execution.
  # A random suffix keeps the suite repeatable against a database that already holds earlier runs.
  Scenario Outline: A visitor registers - <username>
    * def uniqueUsername = '<username>-' + java.util.UUID.randomUUID().toString().substring(0, 6)
    Given path 'api/v1/authentication/sign-up'
    And request { username: '#(uniqueUsername)', password: '<password>' }
    When method post
    Then status 201
    And match response.id == '#number'
    And match response.username == uniqueUsername
    And match response.roles == ['ROLE_USER']

    Examples:
      | read('classpath:com/safestep/apitests/authentication/data/users-batch.json') |

  Scenario: Public registration never grants the admin role
    * def username = 'karate-sneaky-' + java.util.UUID.randomUUID().toString().substring(0, 8)
    Given path 'api/v1/authentication/sign-up'
    And request { username: '#(username)', password: 'SecurePass123!', roles: ['ROLE_ADMIN'] }
    When method post
    Then status 201
    And match response.roles == ['ROLE_USER']

  Scenario: A registered user signs in and receives both tokens
    * def player = call read('classpath:com/safestep/apitests/helpers/register-and-sign-in.feature')
    Given path 'api/v1/authentication/sign-in'
    And request { username: '#(player.username)', password: '#(player.password)' }
    When method post
    Then status 200
    And match response.token == '#string'
    And match response.refreshToken == '#string'
    And match response.roles contains 'ROLE_USER'

  Scenario: A refresh token can only be used once
    * def player = call read('classpath:com/safestep/apitests/helpers/register-and-sign-in.feature')
    Given path 'api/v1/authentication/sign-in'
    And request { username: '#(player.username)', password: '#(player.password)' }
    When method post
    Then status 200
    * def firstRefresh = response.refreshToken
    Given path 'api/v1/authentication/refresh-token'
    And request { refreshToken: '#(firstRefresh)' }
    When method post
    Then status 200
    And match response.refreshToken != firstRefresh
    Given path 'api/v1/authentication/refresh-token'
    And request { refreshToken: '#(firstRefresh)' }
    When method post
    Then status 400

  Scenario: Registering the same username twice is a conflict
    * def player = call read('classpath:com/safestep/apitests/helpers/register-and-sign-in.feature')
    Given path 'api/v1/authentication/sign-up'
    And request { username: '#(player.username)', password: 'SecurePass123!' }
    When method post
    Then status 409

  Scenario Outline: Invalid sign-up data is rejected - <description>
    Given path 'api/v1/authentication/sign-up'
    And request { username: '<username>', password: '<password>' }
    When method post
    Then status 400
    And match response.code == 'VALIDATION_ERROR'

    Examples:
      | description        | username | password     |
      | username too short | ab       | SecurePass1! |
      | password too short | validuser| short        |

  Scenario: A wrong password is refused
    * def player = call read('classpath:com/safestep/apitests/helpers/register-and-sign-in.feature')
    Given path 'api/v1/authentication/sign-in'
    And request { username: '#(player.username)', password: 'WrongPass123!' }
    When method post
    Then status 400
