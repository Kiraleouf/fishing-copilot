Feature: Fish Management

  Background:
    Given the user "fisherman1" is logged in with a valid session
    And the user has an active fishing session named "Test Session"

  Scenario: Add a fish to a rod
    Given the session has a rod named "test rod"
    And the rod has 0 fish
    When I add a fish to the rod
    Then the fish count should be 1
    And the fish should be stored in the database
    And the response should contain the updated fish count

  Scenario: Add multiple fish to a rod
    Given the session has a rod named "test rod 2"
    And the rod has 0 fish
    When I add a fish to the rod
    And I add a fish to the rod
    And I add a fish to the rod
    Then the fish count should be 3

  Scenario: Delete a fish from a rod
    Given the session has a rod named "test rod 3"
    And the rod has 5 fish
    When I delete a fish from the rod
    Then the fish count should be 4
    And the most recent fish should be removed

  Scenario: Delete multiple fish from a rod
    Given the session has a rod named "test rod 4"
    And the rod has 10 fish
    When I delete a fish from the rod
    And I delete a fish from the rod
    And I delete a fish from the rod
    Then the fish count should be 7

  Scenario: Cannot delete fish when rod is empty
    Given the session has a rod named "test rod 5"
    And the rod has 0 fish
    When I try to delete a fish from the rod

  Scenario: Fish count is tracked correctly across operations
    Given the session has a rod named "test rod 6"
    And the rod has 0 fish
    When I add a fish to the rod
    And I add a fish to the rod
    And I add a fish to the rod
    And I delete a fish from the rod
    And I add a fish to the rod
    Then the fish count should be 3

  Scenario: Fish persist after session operations
    Given the session has a rod named "test rod 7"
    And the rod has 5 fish
    When I retrieve the rod details
    Then the rod should still have 5 fish
    And each fish should have a timestamp

