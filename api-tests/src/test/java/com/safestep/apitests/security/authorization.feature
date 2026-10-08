@security @admin @US57 @US58
Feature: Authorization rules of the API

  Background:
    * url karate.properties['api.baseUrl']
    * def admin = call read('classpath:com/safestep/apitests/helpers/sign-in-admin.feature')
    * def player = call read('classpath:com/safestep/apitests/helpers/register-and-sign-in.feature')

  Scenario: Protected endpoints reject anonymous requests
    Given path 'api/v1/commerce/products'
    When method get
    Then status 401

  Scenario Outline: Administrator-only endpoints reject regular players - <method> <path>
    Given path '<path>'
    And header Authorization = 'Bearer ' + player.token
    When method <method>
    Then status 403

    Examples:
      | method | path                                  |
      | get    | api/v1/users                          |
      | get    | api/v1/roles                          |
      | delete | api/v1/commerce/products/not-present  |
      | delete | api/v1/gamification/missions/not-here |
      | delete | api/v1/simulations/not-present        |

  Scenario: An administrator can list users and roles
    Given path 'api/v1/users'
    And header Authorization = 'Bearer ' + admin.token
    When method get
    Then status 200
    And match response == '#[_ > 0]'
    And match response[*].username contains admin.username
    Given path 'api/v1/roles'
    And header Authorization = 'Bearer ' + admin.token
    When method get
    Then status 200
    And match response[*].name contains 'ROLE_ADMIN'

  Scenario: An administrator promotes a player and the change is visible
    Given path 'api/v1/users', player.userId, 'roles'
    And header Authorization = 'Bearer ' + admin.token
    And request { roles: ['ROLE_USER', 'ROLE_INSTRUCTOR'] }
    When method put
    Then status 200
    And match response.roles contains 'ROLE_INSTRUCTOR'
    Given path 'api/v1/users', player.userId
    And header Authorization = 'Bearer ' + admin.token
    When method get
    Then status 200
    And match response.roles contains only ['ROLE_USER', 'ROLE_INSTRUCTOR']

  Scenario: An administrator cannot remove their own administrator role
    Given path 'api/v1/users', admin.userId, 'roles'
    And header Authorization = 'Bearer ' + admin.token
    And request { roles: ['ROLE_USER'] }
    When method put
    Then status 422
    And match response.code == 'BUSINESS_RULE_VIOLATION'
