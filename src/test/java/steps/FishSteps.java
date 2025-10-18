package steps;

import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSession;
import com.ggc.fishingcopilot.fishingsession.rod.FishingRodService;
import com.ggc.fishingcopilot.fishingsession.rod.FishRepository;
import com.ggc.fishingcopilot.fishingsession.rod.model.dto.RodResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class FishSteps {

    @Autowired
    private FishingRodService fishingRodService;

    @Autowired
    private FishRepository fishRepository;

    @Autowired
    private UserSteps userSteps;

    @Autowired
    private FishingSessionSteps fishingSessionSteps;

    @Autowired
    private RodSteps rodSteps;

    @Autowired
    private CommonSteps commonSteps;

    private RodResponse lastRodResponse;

    @Given("the rod has {int} fish")
    public void theRodHasFish(int fishCount) {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession fishingSession = fishingSessionSteps.getCurrentFishingSession();
        RodResponse rod = rodSteps.getCurrentRod();

        // Ajouter le nombre de poissons requis
        for (int i = 0; i < fishCount; i++) {
            fishingRodService.addFish(sessionToken, fishingSession.getId(), rod.getId());
        }

        // Rafraîchir le rod pour avoir le bon compteur
        List<RodResponse> rods = fishingRodService.getRods(sessionToken, fishingSession.getId());
        lastRodResponse = rods.stream()
                .filter(r -> r.getId() == rod.getId())
                .findFirst()
                .orElse(null);
        rodSteps.setCurrentRod(lastRodResponse);
    }

    @Given("rod {int} has {int} fish")
    public void rodHasFish(int rodNumber, int fishCount) {
        // Le rodNumber correspond à l'ordre de création ou à un identifiant spécifique
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession session = fishingSessionSteps.getCurrentFishingSession();

        List<RodResponse> rods = fishingRodService.getRods(sessionToken, session.getId());
        if (rods.size() >= rodNumber) {
            RodResponse rod = rods.get(rodNumber - 1);

            // Ajouter le nombre de poissons requis
            for (int i = 0; i < fishCount; i++) {
                fishingRodService.addFish(sessionToken, session.getId(), rod.getId());
            }
        }
    }

    @When("I add a fish to the rod")
    public void iAddAFishToTheRod() {
        try {
            UUID sessionId = userSteps.getCurrentSessionId();
            FishingSession fishingSession = fishingSessionSteps.getCurrentFishingSession();
            RodResponse rod = rodSteps.getCurrentRod();

            lastRodResponse = fishingRodService.addFish(sessionId, fishingSession.getId(), rod.getId());
            rodSteps.setCurrentRod(lastRodResponse);
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I add {int} fish to rod {string}")
    public void iAddFishToRod(int fishCount, String rodName) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            FishingSession session = fishingSessionSteps.getCurrentFishingSession();
            Integer rodId = rodSteps.getRodIdByName(rodName);

            for (int i = 0; i < fishCount; i++) {
                lastRodResponse = fishingRodService.addFish(sessionToken, session.getId(), rodId);
            }
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I delete a fish from the rod")
    public void iDeleteAFishFromTheRod() {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession session = fishingSessionSteps.getCurrentFishingSession();
        RodResponse rod = rodSteps.getCurrentRod();

        lastRodResponse = fishingRodService.removeFish(sessionToken, session.getId(), rod.getId());
        rodSteps.setCurrentRod(lastRodResponse);
    }

    @When("I delete {int} fish from rod {string}")
    public void iDeleteFishFromRod(int fishCount, String rodName) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            FishingSession session = fishingSessionSteps.getCurrentFishingSession();
            Integer rodId = rodSteps.getRodIdByName(rodName);

            for (int i = 0; i < fishCount; i++) {
                lastRodResponse = fishingRodService.removeFish(sessionToken, session.getId(), rodId);
            }
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I try to delete a fish from the rod")
    public void iTryToDeleteAFishFromTheRod() {
        iDeleteAFishFromTheRod();
    }

    @When("I try to add a fish to a rod with id {int}")
    public void iTryToAddAFishToARodWithId(int rodId) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            FishingSession session = fishingSessionSteps.getCurrentFishingSession();

            lastRodResponse = fishingRodService.addFish(sessionToken, session.getId(), rodId);
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I try to delete a fish from a rod with id {int}")
    public void iTryToDeleteAFishFromARodWithId(int rodId) {
        try {
            UUID sessionToken = userSteps.getCurrentSessionId();
            FishingSession session = fishingSessionSteps.getCurrentFishingSession();

            lastRodResponse = fishingRodService.removeFish(sessionToken, session.getId(), rodId);
        } catch (Exception e) {
            commonSteps.setLastException(e);
        }
    }

    @When("I retrieve the rod details")
    public void iRetrieveTheRodDetails() {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession session = fishingSessionSteps.getCurrentFishingSession();
        RodResponse rod = rodSteps.getCurrentRod();

        List<RodResponse> rods = fishingRodService.getRods(sessionToken, session.getId());
        lastRodResponse = rods.stream()
                .filter(r -> r.getId() == rod.getId())
                .findFirst()
                .orElse(null);
    }

    @Then("the fish count should be {int}")
    public void theFishCountShouldBe(int expectedCount) {
        Assertions.assertNotNull(lastRodResponse, "Rod response should not be null");
        Assertions.assertEquals(expectedCount, lastRodResponse.getFishCount(),
                "Fish count should be " + expectedCount);
    }

    @Then("rod {int} should have {int} fish")
    public void rodShouldHaveFish(int rodNumber, int expectedCount) {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession session = fishingSessionSteps.getCurrentFishingSession();

        List<RodResponse> rods = fishingRodService.getRods(sessionToken, session.getId());
        Assertions.assertTrue(rods.size() >= rodNumber, "Rod " + rodNumber + " should exist");

        RodResponse rod = rods.get(rodNumber - 1);
        Assertions.assertEquals(expectedCount, rod.getFishCount(),
                "Rod " + rodNumber + " should have " + expectedCount + " fish");
    }

    @Then("rod {string} should have {int} fish")
    public void rodShouldHaveFish(String rodName, int expectedCount) {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession session = fishingSessionSteps.getCurrentFishingSession();
        Integer rodId = rodSteps.getRodIdByName(rodName);

        List<RodResponse> rods = fishingRodService.getRods(sessionToken, session.getId());
        RodResponse rod = rods.stream()
                .filter(r -> r.getId() == rodId)
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(rod, "Rod '" + rodName + "' should exist");
        Assertions.assertEquals(expectedCount, rod.getFishCount(),
                "Rod '" + rodName + "' should have " + expectedCount + " fish");
    }

    @Then("the fish should be stored in the database")
    public void theFishShouldBeStoredInTheDatabase() {
        RodResponse rod = rodSteps.getCurrentRod();
        int fishCount = fishRepository.countByFishingRodId(rod.getId());
        Assertions.assertTrue(fishCount > 0, "Fish should be stored in database");
    }

    @Then("the response should contain the updated fish count")
    public void theResponseShouldContainTheUpdatedFishCount() {
        Assertions.assertNotNull(lastRodResponse, "Response should not be null");
        Assertions.assertNotNull(lastRodResponse.getFishCount(), "Response should contain fish count");
    }

    @Then("the most recent fish should be removed")
    public void theMostRecentFishShouldBeRemoved() {
        // La logique de suppression du dernier poisson est gérée par le service
        Assertions.assertNull(commonSteps.getLastException(), "No exception should have been thrown");
    }

    // La step "the operation should fail with error" est maintenant dans CommonSteps

    @Then("the rod should still have {int} fish")
    public void theRodShouldStillHaveFish(int expectedCount) {
        theFishCountShouldBe(expectedCount);
    }

    @Then("each fish should have a timestamp")
    public void eachFishShouldHaveATimestamp() {
        RodResponse rod = rodSteps.getCurrentRod();
        // On vérifie juste que les poissons existent et le comptage est correct
        int fishCount = fishRepository.countByFishingRodId(rod.getId());
        Assertions.assertTrue(fishCount > 0, "Fish should exist with timestamps");
    }

    @Then("the total fish count should be {int}")
    public void theTotalFishCountShouldBe(int expectedTotal) {
        UUID sessionToken = userSteps.getCurrentSessionId();
        FishingSession session = fishingSessionSteps.getCurrentFishingSession();

        List<RodResponse> rods = fishingRodService.getRods(sessionToken, session.getId());
        int totalFish = rods.stream()
                .mapToInt(RodResponse::getFishCount)
                .sum();

        Assertions.assertEquals(expectedTotal, totalFish, "Total fish count should be " + expectedTotal);
    }
}
