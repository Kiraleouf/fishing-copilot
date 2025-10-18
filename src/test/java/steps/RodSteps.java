package steps;

import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSession;
import com.ggc.fishingcopilot.fishingsession.rod.FishingRodService;
import com.ggc.fishingcopilot.fishingsession.rod.model.dto.RodResponse;
import com.ggc.fishingcopilot.fishingsession.rod.FishingRodRepository;
import com.ggc.fishingcopilot.fishingsession.rod.model.entity.FishingRod;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RodSteps {

    @Autowired
    private FishingRodService fishingRodService;

    @Autowired
    private FishingRodRepository fishingRodRepository;

    @Autowired
    private UserSteps userSteps;

    @Autowired
    private FishingSessionSteps fishingSessionSteps;

    @Autowired
    private CommonSteps commonSteps;

    private List<RodResponse> rodList;
    private RodResponse currentRod;
    private Map<String, Integer> rodIdsByName = new HashMap<>();
    private Map<Integer, RodResponse> rodsById = new HashMap<>();

    @Given("the session has no rods")
    public void theSessionHasNoRods() {
        // Les rods seront vides par défaut
        rodIdsByName.clear();
        rodsById.clear();
    }

    @Given("the session has a rod named {string}")
    public void theSessionHasARodNamed(String rodName) {
        UUID sessionId = userSteps.getCurrentSessionId();
        FishingSession fishingSession = fishingSessionSteps.getCurrentFishingSession();

        FishingRod rod = fishingRodService.addRod(sessionId, fishingSession.getId(), rodName);
        rodIdsByName.put(rodName, rod.getId());
        currentRod = new RodResponse(rod.getId(), rod.getName(), 0);
        rodsById.put(rod.getId(), currentRod);
    }

    @Given("the session has {int} rods named {string} and {string}")
    public void theSessionHasRodsNamedAnd(int count, String rod1Name, String rod2Name) {
        theSessionHasARodNamed(rod1Name);
        theSessionHasARodNamed(rod2Name);
    }

    @Given("the session has a rod named {string} with id {int}")
    public void theSessionHasARodNamedWithId(String rodName, int rodId) {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession session = fishingSessionSteps.getCurrentFishingSession();

        FishingRod rod = fishingRodService.addRod(sessionToken, session.getId(), rodName);
        rodIdsByName.put(rodName, rodId);
        currentRod = new RodResponse(rodId, rod.getName(), 0);
        rodsById.put(rodId, currentRod);
    }

    @When("I add a rod named {string} to the session")
    public void iAddARodNamedToTheSession(String rodName) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            FishingSession session = fishingSessionSteps.getCurrentFishingSession();

            FishingRod rod = fishingRodService.addRod(sessionToken, session.getId(), rodName);
            currentRod = new RodResponse(rod.getId(), rod.getName(), 0);
            rodIdsByName.put(rodName, currentRod.getId());
            rodsById.put(currentRod.getId(), currentRod);
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I try to add a rod named {string} to a non-existent session")
    public void iTryToAddARodNamedToANonExistentSession(String rodName) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            int nonExistentSessionId = 99999;

            FishingRod rod = fishingRodService.addRod(sessionToken, nonExistentSessionId, rodName);
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I request the list of rods for the session")
    public void iRequestTheListOfRodsForTheSession() {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            FishingSession session = fishingSessionSteps.getCurrentFishingSession();

            rodList = fishingRodService.getRods(sessionToken, session.getId());
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I delete the rod from the session")
    public void iDeleteTheRodFromTheSession() {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            FishingSession session = fishingSessionSteps.getCurrentFishingSession();

            fishingRodService.deleteRod(sessionToken, session.getId(), currentRod.getId());
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I try to delete a rod with id {int}")
    public void iTryToDeleteARodWithId(int rodId) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            FishingSession session = fishingSessionSteps.getCurrentFishingSession();

            fishingRodService.deleteRod(sessionToken, session.getId(), rodId);
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @Then("the rod should be created successfully")
    public void theRodShouldBeCreatedSuccessfully() {
        Assertions.assertNull(commonSteps.getLastException(), "No exception should have been thrown");
        Assertions.assertNotNull(currentRod, "Rod should be created");
        Assertions.assertNotNull(currentRod.getId(), "Rod should have an ID");
    }

    @Then("the rod should belong to the session")
    public void theRodShouldBelongToTheSession() {
        Assertions.assertNotNull(currentRod, "Rod should exist");
        FishingRod dbRod = fishingRodRepository.findById(currentRod.getId()).orElse(null);
        Assertions.assertNotNull(dbRod, "Rod should be in database");

        FishingSession session = fishingSessionSteps.getCurrentFishingSession();
        Assertions.assertEquals(session.getId(), dbRod.getFishingSession().getId(),
                                "Rod should belong to the current session");
    }

    @Then("the rod should have {int} fish initially")
    public void theRodShouldHaveFishInitially(int expectedFishCount) {
        Assertions.assertNotNull(currentRod, "Rod should exist");
        Assertions.assertEquals(expectedFishCount, currentRod.getFishCount(),
                                "Rod should have " + expectedFishCount + " fish");
    }

    @Then("the session should have {int} rods")
    public void theSessionShouldHaveRods(int expectedRodCount) {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession session = fishingSessionSteps.getCurrentFishingSession();

        List<RodResponse> rods = fishingRodService.getRods(sessionToken, session.getId());
        Assertions.assertEquals(expectedRodCount, rods.size(),
                                "Session should have " + expectedRodCount + " rods");
    }

    @Then("I should receive {int} rods")
    public void iShouldReceiveRods(int expectedRodCount) {
        Assertions.assertNotNull(rodList, "Rod list should not be null");
        Assertions.assertEquals(expectedRodCount, rodList.size(),
                                "Should receive " + expectedRodCount + " rods");
    }

    @Then("each rod should have a name and fish count")
    public void eachRodShouldHaveANameAndFishCount() {
        Assertions.assertNotNull(rodList, "Rod list should not be null");
        for (RodResponse rod : rodList) {
            Assertions.assertNotNull(rod.getName(), "Rod should have a name");
            Assertions.assertNotNull(rod.getFishCount(), "Rod should have a fish count");
        }
    }

    @Then("the rod should be removed successfully")
    public void theRodShouldBeRemovedSuccessfully() {
        Assertions.assertNull(commonSteps.getLastException(), "No exception should have been thrown");
    }

    @Then("the rod should no longer exist in the database")
    public void theRodShouldNoLongerExistInTheDatabase() {
        FishingRod dbRod = fishingRodRepository.findById(currentRod.getId()).orElse(null);
        Assertions.assertNull(dbRod, "Rod should not exist in database");
    }

    // La step "the operation should fail with error" est maintenant dans CommonSteps - supprimée d'ici

    @Then("the rod name should be {string}")
    public void theRodNameShouldBe(String expectedName) {
        Assertions.assertNotNull(currentRod, "Rod should exist");
        Assertions.assertEquals(expectedName, currentRod.getName(), "Rod name should match");
    }

    // Méthodes utilitaires pour les autres steps
    public RodResponse getCurrentRod() {
        return currentRod;
    }

    public RodResponse getRodByName(String rodName) {
        Integer rodId = rodIdsByName.get(rodName);
        if (rodId != null) {
            return rodsById.get(rodId);
        }
        return null;
    }

    public Integer getRodIdByName(String rodName) {
        return rodIdsByName.get(rodName);
    }

    public void setCurrentRod(RodResponse rod) {
        this.currentRod = rod;
    }
}
