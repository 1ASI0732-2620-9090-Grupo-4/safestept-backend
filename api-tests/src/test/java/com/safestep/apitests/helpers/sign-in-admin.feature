@ignore
Feature: Reusable helper - sign in as the seeded administrator

  Scenario: Sign in as admin
    * def adminUsername = karate.properties['api.admin.username']
    * def adminPassword = karate.properties['api.admin.password']
    Given url karate.properties['api.baseUrl']
    And path 'api/v1/authentication/sign-in'
    And request { username: '#(adminUsername)', password: '#(adminPassword)' }
    When method post
    Then status 200
    * def token = response.token
    * def userId = response.id
    * def username = response.username
