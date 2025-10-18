Feature: Complete Fishing Session Workflow

  Scenario: Complete fishing workflow from login to session close
    Given the user "fisherman_test_account_1" is registered with password "password123"
    When I login with username "fisherman_test_account_1" and password "password123"
    Then I should receive a valid session token

    When I create a new fishing session with name "Afternoon Carp Fishing"
    Then the session should be created successfully
    And the session status should be "IN_PROGRESS"

    When I add a rod named "Left Rod" to the session
    And I add a rod named "Right Rod" to the session
    Then the session should have 2 rods

    When I add 3 fish to rod "Left Rod"
    And I add 5 fish to rod "Right Rod"
    Then rod "Left Rod" should have 3 fish
    And rod "Right Rod" should have 5 fish

    When I delete 1 fish from rod "Right Rod"
    Then rod "Right Rod" should have 4 fish

    When I close the current fishing session
    Then the session status should be "CLOSED"
    And the session should have an end date

    When I request the session history with page 0 and size 10
    Then I should receive 1 fishing session
    And the session should contain 2 rods
    And the total fish count should be 7
