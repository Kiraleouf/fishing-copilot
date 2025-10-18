Feature: Fishing Rod Management

  Scenario: Add a rod to the session
    Given the user "fisherman_test_1" is logged in with a valid session
    And the user has an active fishing session named "Test Session"
    And the session has no rods
    When I add a rod named "Canne 1" to the session
    Then the rod should be created successfully
    And the rod should belong to the session
    And the rod should have 0 fish initially

  Scenario: Add multiple rods to the session
    Given the user "fisherman_test_2" is logged in with a valid session
    And the user has an active fishing session named "Test Session"
    And the session has no rods
    When I add a rod named "Canne Gauche" to the session
    And I add a rod named "Canne Droite" to the session
    And I add a rod named "Canne Centrale" to the session
    Then the session should have 3 rods

  Scenario: List rods for a session
    Given the user "fisherman_test_3" is logged in with a valid session
    And the user has an active fishing session named "Test Session"
    And the session has no rods
    Given the session has 2 rods named "Rod 1" and "Rod 2"
    When I request the list of rods for the session
    Then I should receive 2 rods
    And each rod should have a name and fish count

  Scenario: Delete a rod from the session
    Given the user "fisherman_test_4" is logged in with a valid session
    And the user has an active fishing session named "Test Session"
    And the session has no rods
    Given the session has a rod named "Rod to Delete"
    When I delete the rod from the session
    Then the rod should be removed successfully
    And the rod should no longer exist in the database

  Scenario: Add rod with custom name
    Given the user "fisherman_test_5" is logged in with a valid session
    And the user has an active fishing session named "Test Session"
    And the session has no rods
    When I add a rod named "Feeder - Gauche" to the session
    Then the rod name should be "Feeder - Gauche"
    And the rod should be created successfully

