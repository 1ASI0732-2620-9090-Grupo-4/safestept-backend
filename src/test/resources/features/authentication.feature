@authentication @US01 @US02
Feature: Account registration and sign in
  As a visitor of SafeStep
  I want to create an account and sign in
  So that my training progress is saved and protected

  Scenario: A visitor registers a new account
    When a visitor registers with a new username and a valid password
    Then the response status is 201
    And the new account only has the role "ROLE_USER"

  Scenario: A visitor cannot grant themselves the admin role
    When a visitor registers requesting the role "ROLE_ADMIN"
    Then the response status is 201
    And the new account only has the role "ROLE_USER"

  Scenario: A registered user signs in with valid credentials
    Given a registered user
    When the user signs in with the right password
    Then the response status is 200
    And the response contains an access token

  Scenario: A user cannot register the same username twice
    Given a registered user
    When a visitor registers with the username of the registered user
    Then the response status is 409

  Scenario Outline: Invalid registration data is rejected
    When a visitor registers with the username "<username>" and the password "<password>"
    Then the response status is 400

    Examples:
      | username | password      |
      | ab       | SecurePass1!  |
      | valid    | short         |

  Scenario Outline: A registered user cannot sign in with invalid credentials
    Given a registered user
    When the user signs in with the password "<password>"
    Then the response status is 400

    Examples:
      | password      |
      | WrongPass123! |
      | short         |
