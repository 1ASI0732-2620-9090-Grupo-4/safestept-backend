@admin @roles @US57 @US58
Feature: Role management by administrators
  As an administrator of SafeStep
  I want to assign roles to users from the admin dashboard
  So that only trusted people can manage the platform data

  Background:
    Given an administrator is signed in
    And a regular player exists

  Scenario: An administrator grants the instructor role
    When the administrator assigns the roles "ROLE_USER,ROLE_INSTRUCTOR" to the regular player
    Then the response status is 200
    And the regular player has the roles "ROLE_USER,ROLE_INSTRUCTOR"

  Scenario: An administrator promotes a player to administrator
    When the administrator assigns the roles "ROLE_ADMIN" to the regular player
    Then the response status is 200
    And the regular player has the roles "ROLE_ADMIN"

  Scenario: A regular player cannot manage roles
    When the regular player tries to assign the roles "ROLE_ADMIN" to themselves
    Then the response status is 403

  Scenario: An anonymous visitor cannot list the users
    When an anonymous visitor lists the users
    Then the response status is 401

  Scenario: An administrator cannot remove their own administrator role
    Given another administrator exists
    When the administrator removes their own administrator role
    Then the response status is 422
    And the response contains the error code "BUSINESS_RULE_VIOLATION"

  Scenario: An administrator cannot assign a role that does not exist
    When the administrator assigns the roles "ROLE_ROOT" to the regular player
    Then the response status is 400
