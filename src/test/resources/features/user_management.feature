Feature: User Management

  Scenario: Register a new user
    Given the user "kira" has no account
    When I register a new user with valid details
    Then the user is known to the system
