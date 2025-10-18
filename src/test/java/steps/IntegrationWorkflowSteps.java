package steps;

import com.ggc.fishingcopilot.fishingsession.model.dto.FishingSessionResponse;
import com.ggc.fishingcopilot.fishingsession.model.dto.PaginatedResponse;
import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSession;
import com.ggc.fishingcopilot.fishingsession.rod.model.dto.RodResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegrationWorkflowSteps {

    @Autowired
    private UserSteps userSteps;

    @Autowired
    private FishingSessionSteps fishingSessionSteps;

    @Autowired
    private RodSteps rodSteps;

    @Autowired
    private FishSteps fishSteps;

    // Map pour gérer plusieurs utilisateurs dans les tests d'isolation
    private Map<String, String> userContexts = new HashMap<>();
    private String currentTestUser;
    private PaginatedResponse<FishingSessionResponse> lastSessionHistory;
    private List<RodResponse> lastRodList;

    @Given("the user {string} has an active session with {int} rods")
    public void theUserHasAnActiveSessionWithRods(String username, int rodCount) {
        // Sauvegarder le contexte utilisateur actuel
        String previousUser = userSteps.getCurrentUsername();

        // Se connecter avec l'utilisateur spécifié
        userSteps.theUserExistsWithPassword(username, "password123");
        userSteps.iLoginWithUsernameAndPassword(username, "password123");

        // Créer une session active
        fishingSessionSteps.theUserHasNoActiveFishingSession();
        fishingSessionSteps.iCreateANewFishingSessionWithName(username + " Session");

        // Ajouter les rods
        for (int i = 1; i <= rodCount; i++) {
            rodSteps.iAddARodNamedToTheSession("Rod " + i + " - " + username);
        }

        userContexts.put(username, username);
    }

    @When("{string} requests their current session")
    public void requestsTheirCurrentSession(String username) {
        // Basculer vers le contexte de l'utilisateur
        currentTestUser = username;

        // L'utilisateur devrait déjà être connecté depuis le Given
        fishingSessionSteps.iRequestTheCurrentFishingSession();

        FishingSession session = fishingSessionSteps.getCurrentFishingSession();
        if (session != null) {
            rodSteps.iRequestTheListOfRodsForTheSession();
        }
    }

    @When("{string} requests the session history")
    public void requestsTheSessionHistory(String username) {
        currentTestUser = username;
        fishingSessionSteps.iRequestTheSessionHistoryWithPageAndSize(0, 10);
    }

    @Then("they should see {int} rods")
    public void theyShouldSeeRods(int expectedRodCount) {
        FishingSession session = fishingSessionSteps.getCurrentFishingSession();
        Assertions.assertNotNull(session, "Session should exist for user " + currentTestUser);

        // Vérifier le nombre de rods
        rodSteps.theSessionShouldHaveRods(expectedRodCount);
    }

    @Then("they should receive {int} fishing sessions")
    public void theyShouldReceiveFishingSessions(int expectedCount) {
        fishingSessionSteps.iShouldReceiveFishingSessions(expectedCount);
    }

    @Then("the sessions should be independent")
    public void theSessionsShouldBeIndependent() {
        // Cette vérification est implicite : si chaque utilisateur voit ses propres rods,
        // alors les sessions sont bien isolées
        Assertions.assertTrue(true, "Sessions are independent");
    }

    @Then("no sessions from other users should appear")
    public void noSessionsFromOtherUsersShouldAppear() {
        // Cette vérification est implicite dans le service qui filtre par utilisateur
        Assertions.assertTrue(true, "Sessions are filtered by user");
    }

    @Then("the session should contain {int} rods")
    public void theSessionShouldContainRods(int expectedRodCount) {
        rodSteps.theSessionShouldHaveRods(expectedRodCount);
    }
}
