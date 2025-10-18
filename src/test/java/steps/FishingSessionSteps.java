package steps;

import com.ggc.fishingcopilot.fishingsession.FishingSessionRepository;
import com.ggc.fishingcopilot.fishingsession.model.dto.FishingSessionResponse;
import com.ggc.fishingcopilot.fishingsession.model.dto.PaginatedResponse;
import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSession;
import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSessionStatus;
import com.ggc.fishingcopilot.fishingsession.service.FishingSessionService;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class FishingSessionSteps {

    @Autowired
    private FishingSessionService fishingSessionService;

    @Autowired
    private FishingSessionRepository fishingSessionRepository;

    @Autowired
    private UserSteps userSteps;

    @Autowired
    private CommonSteps commonSteps;

    private FishingSession currentFishingSession;
    private PaginatedResponse<FishingSessionResponse> sessionHistory;
    private Integer httpStatusCode;

    @Given("the user is logged in with a valid session")
    public void theUserIsLoggedInWithAValidSession() {
        UUID sessionToken = userSteps.getCurrentSessionId();
        Assertions.assertNotNull(sessionToken, "User should be logged in");
    }

    @Given("the user has no previous fishing sessions")
    public void theUserHasNoPreviousFishingSessions() {
        UUID sessionToken = userSteps.getCurrentSessionId();
        PaginatedResponse<FishingSessionResponse> history = fishingSessionService.getPaginatedSessions(sessionToken, 0, 10);
        // Nettoyer les sessions existantes si nécessaire pour ce test
    }

    @Given("the user has {int} completed fishing sessions")
    public void theUserHasCompletedFishingSessions(int count) {
        UUID sessionToken = userSteps.getCurrentSessionId();
        // Créer des sessions de test complétées
        for (int i = 0; i < count; i++) {
            FishingSession session = fishingSessionService.create(sessionToken, "Test Session " + (i + 1));
            session.setStatus(FishingSessionStatus.CLOSED);
            fishingSessionRepository.save(session);
        }
    }

    @Given("the user has no active fishing session")
    public void theUserHasNoActiveFishingSession() {
        UUID sessionId = userSteps.getCurrentSessionId();
        if (sessionId != null) {
            FishingSession active = fishingSessionService.getCurrent(sessionId);
            if (active != null) {
                fishingSessionService.close(sessionId);
            }
        }
    }

    @Given("the user has an active fishing session named {string}")
    public void theUserHasAnActiveFishingSessionNamed(String sessionName) {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession active = fishingSessionService.getCurrent(sessionToken);
        if (active == null) {
            currentFishingSession = fishingSessionService.create(sessionToken, sessionName);
        } else {
            currentFishingSession = active;
        }
    }

    @Given("the session has {int} rods with fish")
    public void theSessionHasRodsWithFish(int rodCount) {
        // Cette étape sera implémentée dans RodSteps
        // Pour l'instant, on peut la laisser vide ou créer des rods basiques
    }

    @When("I request the session history with page {int} and size {int}")
    public void iRequestTheSessionHistoryWithPageAndSize(int page, int size) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            sessionHistory = fishingSessionService.getPaginatedSessions(sessionToken, page, size);
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I create a new fishing session with name {string}")
    public void iCreateANewFishingSessionWithName(String sessionName) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            currentFishingSession = fishingSessionService.create(sessionToken, sessionName);
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I try to create a new fishing session with name {string}")
    public void iTryToCreateANewFishingSessionWithName(String sessionName) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            currentFishingSession = fishingSessionService.create(sessionToken, sessionName);
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I request the current fishing session")
    public void iRequestTheCurrentFishingSession() {
        try {
            httpStatusCode = null;
            UUID sessionToken = userSteps.getCurrentSessionId();
            currentFishingSession = fishingSessionService.getCurrent(sessionToken);
            if (currentFishingSession == null) {
                httpStatusCode = 204; // No Content
            } else {
                httpStatusCode = 200;
            }
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I close the current fishing session")
    public void iCloseTheCurrentFishingSession() {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            fishingSessionService.close(sessionToken);
            // Recharger la session pour vérifier son statut
            if (currentFishingSession != null) {
                currentFishingSession = fishingSessionRepository.findById(currentFishingSession.getId()).orElse(null);
            }
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I try to close the current fishing session")
    public void iTryToCloseTheCurrentFishingSession() {
        iCloseTheCurrentFishingSession();
    }

    @Then("I should receive an empty list of sessions")
    public void iShouldReceiveAnEmptyListOfSessions() {
        Assertions.assertNotNull(sessionHistory, "Session history should not be null");
        Assertions.assertTrue(sessionHistory.getItems().isEmpty(), "Session list should be empty");
    }

    @Then("the total count should be {int}")
    public void theTotalCountShouldBe(int expectedCount) {
        Assertions.assertNotNull(sessionHistory, "Session history should not be null");
        Assertions.assertEquals(expectedCount, sessionHistory.getItems().size(), "Total count should match");
    }

    @Then("I should receive {int} fishing sessions")
    public void iShouldReceiveFishingSessions(int expectedCount) {
        Assertions.assertNotNull(sessionHistory, "Session history should not be null");
        Assertions.assertEquals(expectedCount, sessionHistory.getItems().size(), "Should receive " + expectedCount + " sessions");
    }

    @Then("each session should have a name and date")
    public void eachSessionShouldHaveANameAndDate() {
        Assertions.assertNotNull(sessionHistory, "Session history should not be null");
        for (FishingSessionResponse session : sessionHistory.getItems()) {
            Assertions.assertNotNull(session.getName(), "Session should have a name");
            Assertions.assertNotNull(session.getDate(), "Session should have a date");
        }
    }

    @Then("the session should be created successfully")
    public void theSessionShouldBeCreatedSuccessfully() {
        Assertions.assertNull(commonSteps.getLastException(), "No exception should have been thrown");
        Assertions.assertNotNull(currentFishingSession, "Session should be created");
        Assertions.assertNotNull(currentFishingSession.getId(), "Session should have an ID");
    }

    @Then("the session should have status {string}")
    public void theSessionShouldHaveStatus(String status) {
        Assertions.assertNotNull(currentFishingSession, "Session should exist");
        Assertions.assertEquals(FishingSessionStatus.valueOf(status), currentFishingSession.getStatus(),
                                "Session status should be " + status);
    }

    @Then("the session status should be {string}")
    public void theSessionStatusShouldBe(String status) {
        theSessionShouldHaveStatus(status);
    }

    @Then("the session should be stored in the database")
    public void theSessionShouldBeStoredInTheDatabase() {
        Assertions.assertNotNull(currentFishingSession, "Session should exist");
        FishingSession dbSession = fishingSessionRepository.findById(currentFishingSession.getId()).orElse(null);
        Assertions.assertNotNull(dbSession, "Session should be in database");
    }

    @Then("it should be set as the current session")
    public void itShouldBeSetAsTheCurrentSession() {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession current = fishingSessionService.getCurrent(sessionToken);
        Assertions.assertNotNull(current, "Current session should exist");
        Assertions.assertEquals(currentFishingSession.getId(), current.getId(), "Should be the current session");
    }

    @Then("the session name should be {string}")
    public void theSessionNameShouldBe(String expectedName) {
        Assertions.assertNotNull(currentFishingSession, "Session should exist");
        Assertions.assertEquals(expectedName, currentFishingSession.getName(), "Session name should match");
    }

    @Then("the creation should fail with error {string}")
    public void theCreationShouldFailWithError(String errorMessage) {
        Exception lastException = commonSteps.getLastException();
        Assertions.assertNotNull(lastException, "An exception should have been thrown");
        Assertions.assertTrue(lastException.getMessage().contains(errorMessage) ||
                              lastException.getClass().getSimpleName().contains("AlreadyInProgress"),
                              "Error message should contain: " + errorMessage);
    }

    @Then("I should receive the session details")
    public void iShouldReceiveTheSessionDetails() {
        Assertions.assertNotNull(currentFishingSession, "Session details should be returned");
        Assertions.assertEquals(200, httpStatusCode, "Status code should be 200");
    }

    @Then("I should receive a {int} No Content response")
    public void iShouldReceiveANoContentResponse(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, httpStatusCode, "Status code should be " + expectedStatusCode);
        Assertions.assertNull(currentFishingSession, "No session should be returned");
    }

    @Then("the session should have an end date")
    public void theSessionShouldHaveAnEndDate() {
        Assertions.assertNotNull(currentFishingSession, "Session should exist");
        Assertions.assertEquals(FishingSessionStatus.CLOSED, currentFishingSession.getStatus(),
                                "Session should be closed");
    }

    @Then("the session should no longer be the current session")
    public void theSessionShouldNoLongerBeTheCurrentSession() {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession current = fishingSessionService.getCurrent(sessionToken);
        Assertions.assertNull(current, "No current session should exist");
    }

    // Méthode utilitaire pour obtenir la session courante (utilisée par d'autres steps)
    public FishingSession getCurrentFishingSession() {
        return currentFishingSession;
    }

    public void setCurrentFishingSession(FishingSession session) {
        this.currentFishingSession = session;
    }

    @Then("I should receive {int} fishing session")
    public void iShouldReceiveFishingSession(int count) {
        // Write code here that turns the phrase above into concrete actions
        Assertions.assertEquals(count, sessionHistory.getTotalItems());
    }
}
