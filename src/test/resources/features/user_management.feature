Feature: User Management

  Scenario: Register a new user
    Given the user "kira" has no account
    When I register a new user with username "kira", password "password123", secret question "Ville natale?" and secret answer "Paris"
    Then the user is known to the system
    And the user "kira" can be found in the database

  Scenario: Register with existing username
    Given the user "existingUser" already exists
    When I try to register with username "existingUser", password "password123", secret question "Question?" and secret answer "Answer"
    Then the registration should fail with error "Username already exists"

  Scenario: Login with valid credentials
    Given the user "testUser" exists with password "myPassword"
    When I login with username "testUser" and password "myPassword"
    Then I should receive a valid session token
    And the user session should be stored in the database

  Scenario: Login with invalid credentials
    Given the user "testUser" exists with password "correctPassword"
    When I login with username "testUser" and password "wrongPassword"
    Then the login should fail with error "Invalid credentials"

  Scenario: Login with non-existent user
    Given the user "ghost" has no account
    When I login with username "ghost" and password "anyPassword"
    Then the login should fail with error "Wrong reques"

  Scenario: Request password reset with secret question
    Given the user "forgetful" exists with secret question "Ville natale?" and secret answer "Paris"
    When I request password reset for username "forgetful" with secret answer "Paris"
    Then a password reset should be authorized
    And I should be able to set a new password

  Scenario: Reset password with correct secret answer
    Given the user "forgetful" exists with secret question "Ville natale?" and secret answer "Paris"
    When I answer the secret question with "Paris"
    And I reset the password to "newPassword456"
    Then the password should be updated successfully

  Scenario: Reset password with incorrect secret answer
    Given the user "forgetful" exists with secret question "Ville natale?" and secret answer "Paris"
    When I answer the secret question with "London"
    Then the reset should fail with error "Wrong answer"
