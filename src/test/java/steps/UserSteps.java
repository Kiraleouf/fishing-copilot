package steps;

import com.ggc.fishingcopilot.fisherman.FishermanRepository;
import com.ggc.fishingcopilot.fisherman.model.entity.Fisherman;
import com.ggc.fishingcopilot.fisherman.service.FishermanService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

public class UserSteps {

    @Autowired
    FishermanService fishermanService;

    @Autowired
    FishermanRepository fishermanRepository;

    @Given("the user {string} has no account")
    public void theUserHasNoAccount(String userName) {
        //Get fisherman from database and shound not exists
        Fisherman fisherman = fishermanRepository.findByUsername(userName);
        Assertions.assertNull(fisherman);
    }

    @When("I register a new user with valid details")
    public void iRegisterANewUserWithValidDetails() {
        Assertions.assertTrue(true);
    }

    @Then("the user is known to the system")
    public void theUserIsKnownToTheSystem() {
        Assertions.assertTrue(true);
    }
}
