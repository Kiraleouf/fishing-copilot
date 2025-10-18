Feature: Fishing Session Management

  Scenario: Get empty session history
    Given the user "fisherman_test_session_1" is logged in with a valid session
    And the user has no previous fishing sessions
    When I request the session history with page 0 and size 10
    Then I should receive an empty list of sessions
    And the total count should be 0

  Scenario: Get session history with existing sessions
    Given the user "fisherman_test_session_2" is logged in with a valid session
    And the user has 5 completed fishing sessions
    When I request the session history with page 0 and size 10
    Then I should receive 5 fishing sessions
    And each session should have a name and date

  Scenario: Get paginated session history
    Given the user "fisherman_test_session_3" is logged in with a valid session
    And the user has 25 completed fishing sessions
    When I request the session history with page 0 and size 10
    Then I should receive 10 fishing sessions
    When I request the session history with page 1 and size 10
    Then I should receive 10 fishing sessions
    When I request the session history with page 2 and size 10
    Then I should receive 5 fishing sessions

  Scenario: Create a new fishing session
    Given the user "fisherman_test_session_4" is logged in with a valid session
    And the user has no active fishing session
    When I create a new fishing session with name "Morning Session"
    Then the session should be created successfully
    And the session should have status "IN_PROGRESS"
    And the session should be stored in the database
    And it should be set as the current session

  Scenario: Create session with custom name
    Given the user "fisherman_test_session_5" is logged in with a valid session
    And the user has no active fishing session
    When I create a new fishing session with name "Lac de Sainte-Croix - Carpe"
    Then the session should be created successfully
    And the session name should be "Lac de Sainte-Croix - Carpe"

  Scenario: Get current active session
    Given the user "fisherman_test_session_6" is logged in with a valid session
    And the user has an active fishing session named "Current Session"
    When I request the current fishing session
    Then I should receive the session details
    And the session status should be "IN_PROGRESS"
    And the session name should be "Current Session"

  Scenario: Close an active fishing session
    Given the user "fisherman_test_session_7" is logged in with a valid session
    Given the user has an active fishing session named "Session to Close"
    And the session has 3 rods with fish
    When I close the current fishing session
    Then the session status should be "CLOSED"
    And the session should have an end date
    And the session should no longer be the current session
