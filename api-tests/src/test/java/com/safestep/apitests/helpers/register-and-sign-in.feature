@ignore
Feature: Reusable helper - register a brand new player and sign in

  Scenario: Register and sign in
    * def baseUrl = karate.properties['api.baseUrl']
    * def username = 'karate-' + java.util.UUID.randomUUID().toString().substring(0, 8)
    * def password = 'SecurePass123!'

    Given url baseUrl
    And path 'api/v1/authentication/sign-up'
    And request { username: '#(username)', password: '#(password)' }
    When method post
    Then status 201

    Given url baseUrl
    And path 'api/v1/authentication/sign-in'
    And request { username: '#(username)', password: '#(password)' }
    When method post
    Then status 200
    * def token = response.token
    * def userId = response.id
