package steps;

import com.ggc.fishingcopilot.fisherman.FishermanRepository;
import com.ggc.fishingcopilot.fisherman.model.entity.Fisherman;
import com.ggc.fishingcopilot.fisherman.service.FishermanService;
import com.ggc.fishingcopilot.session.UserSessionRepository;
import com.ggc.fishingcopilot.session.model.entity.UserSession;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class UserSteps {

    @Autowired
    FishermanService fishermanService;

    @Autowired
    FishermanRepository fishermanRepository;

    @Autowired
    UserSessionRepository userSessionRepository;

    private String currentUsername;
    private String currentPassword;
    private String secretQuestion;
    private String secretAnswer;
    private UUID currentSessionId;
    private Exception lastException;
    private boolean passwordResetAuthorized = false;

    @Given("the user {string} has no account")
    public void theUserHasNoAccount(String userName) {
        Fisherman fisherman = fishermanRepository.findByUsername(userName);
        Assertions.assertNull(fisherman, "User should not exist");
    }

    @Given("the user {string} already exists")
    public void theUserAlreadyExists(String userName) {
        Fisherman existing = fishermanRepository.findByUsername(userName);
        if (existing == null) {
            fishermanService.register(userName, "password123", "Question?", "Answer");
        }
        Fisherman fisherman = fishermanRepository.findByUsername(userName);
        Assertions.assertNotNull(fisherman, "User should exist");
    }

    @Given("the user {string} exists with password {string}")
    public void theUserExistsWithPassword(String userName, String password) {
        Fisherman existing = fishermanRepository.findByUsername(userName);
        if (existing == null) {
            fishermanService.register(userName, password, "Question?", "Answer");
        }
        currentUsername = userName;
        currentPassword = password;
    }

    @Given("the user {string} exists with secret question {string} and secret answer {string}")
    public void theUserExistsWithSecretQuestionAndSecretAnswer(String userName, String question, String answer) {
        Fisherman existing = fishermanRepository.findByUsername(userName);
        if (existing == null) {
            fishermanService.register(userName, "password123", question, answer);
        }
        currentUsername = userName;
        secretQuestion = question;
        secretAnswer = answer;
    }

    @Given("the user {string} exists")
    public void theUserExists(String userName) {
        Fisherman existing = fishermanRepository.findByUsername(userName);
        if (existing == null) {
            fishermanService.register(userName, "password123", "Question?", "Answer");
        }
        currentUsername = userName;
    }

    @Given("the user {string} is registered with password {string}")
    public void theUserIsRegisteredWithPassword(String userName, String password) {
        Fisherman existing = fishermanRepository.findByUsername(userName);
        if (existing == null) {
            fishermanService.register(userName, password, "Question?", "Answer");
        }
        currentUsername = userName;
        currentPassword = password;
    }

    @When("I register a new user with username {string}, password {string}, secret question {string} and secret answer {string}")
    public void iRegisterANewUserWithUsernamePasswordSecretQuestionAndSecretAnswer(String username, String password, String question, String answer) {
        try {
            lastException = null;
            fishermanService.register(username, password, question, answer);
            currentUsername = username;
            currentPassword = password;
            secretQuestion = question;
            secretAnswer = answer;
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I try to register with username {string}, password {string}, secret question {string} and secret answer {string}")
    public void iTryToRegisterWithUsernamePasswordSecretQuestionAndSecretAnswer(String username, String password, String question, String answer) {
        try {
            lastException = null;
            fishermanService.register(username, password, question, answer);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I login with username {string} and password {string}")
    public void iLoginWithUsernameAndPassword(String username, String password) {
        try {
            lastException = null;
            currentSessionId = fishermanService.signIn(username, password);
            currentUsername = username;
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I request password reset for username {string} with secret answer {string}")
    public void iRequestPasswordResetForUsernameWithSecretAnswer(String username, String answer) {
        try {
            lastException = null;
            currentUsername = username;
            String result = fishermanService.secretQuestion(username, answer);
            if ("OK".equals(result)) {
                passwordResetAuthorized = true;
            } else {
                throw new IllegalArgumentException("Incorrect secret answer");
            }
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I answer the secret question with {string}")
    public void iAnswerTheSecretQuestionWith(String answer) {
        try {
            lastException = null;
            String result = fishermanService.secretQuestion(currentUsername, answer);
            if ("OK".equals(result)) {
                passwordResetAuthorized = true;
            } else {
                throw new IllegalArgumentException("Incorrect secret answer");
            }
        } catch (Exception e) {
            lastException = e;
            passwordResetAuthorized = false;
        }
    }

    @When("I reset the password to {string}")
    public void iResetThePasswordTo(String newPassword) {
        try {
            lastException = null;
            if (!passwordResetAuthorized) {
                throw new IllegalArgumentException("Password reset not authorized");
            }
            fishermanService.resetPassword(currentUsername, secretAnswer, newPassword);
            currentPassword = newPassword;
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the user is known to the system")
    public void theUserIsKnownToTheSystem() {
        Fisherman fisherman = fishermanRepository.findByUsername(currentUsername);
        Assertions.assertNotNull(fisherman, "User should be registered");
    }

    @Then("the user {string} can be found in the database")
    public void theUserCanBeFoundInTheDatabase(String userName) {
        Fisherman fisherman = fishermanRepository.findByUsername(userName);
        Assertions.assertNotNull(fisherman, "User should exist in database");
    }

    @Then("the registration should fail with error {string}")
    public void theRegistrationShouldFailWithError(String errorMessage) {
        Assertions.assertNotNull(lastException, "An exception should have been thrown");
        Assertions.assertTrue(lastException.getMessage().contains(errorMessage) ||
                              lastException.getClass().getSimpleName().contains("AlreadyExists"),
                              "Error message should contain: " + errorMessage);
    }

    @Then("I should receive a valid session token")
    public void iShouldReceiveAValidSessionToken() {
        Assertions.assertNotNull(currentSessionId, "Session token should not be null");
    }

    @Then("the user session should be stored in the database")
    public void theUserSessionShouldBeStoredInTheDatabase() {
        UserSession session = userSessionRepository.findById(currentSessionId).orElse(null);
        Assertions.assertNotNull(session, "Session should be stored in database");
    }

    @Then("the login should fail with error {string}")
    public void theLoginShouldFailWithError(String errorMessage) {
        Assertions.assertNotNull(lastException, "An exception should have been thrown");
        Assertions.assertTrue(lastException.getMessage().contains(errorMessage) ||
                              lastException.getClass().getSimpleName().contains("Invalid") ||
                              lastException.getClass().getSimpleName().contains("NotFound"),
                              "Error message should contain: " + errorMessage);
    }

    @Then("a password reset should be authorized")
    public void aPasswordResetShouldBeAuthorized() {
        Assertions.assertTrue(passwordResetAuthorized, "Password reset should be authorized");
    }

    @Then("I should be able to set a new password")
    public void iShouldBeAbleToSetANewPassword() {
        Assertions.assertTrue(passwordResetAuthorized, "Should be able to set new password");
    }

    @Then("the password should be updated successfully")
    public void thePasswordShouldBeUpdatedSuccessfully() {
        Assertions.assertNull(lastException, "No exception should have been thrown");
        Assertions.assertTrue(passwordResetAuthorized, "Password reset should be authorized");
    }

    @Then("the reset should fail with error {string}")
    public void theResetShouldFailWithError(String errorMessage) {
        Assertions.assertNotNull(lastException, "An exception should have been thrown");
        Assertions.assertTrue(lastException.getMessage().contains(errorMessage),
                              "Error message should contain: " + errorMessage);
    }

    @Given("the user {string} is logged in with a valid session")
    public void theUserIsLoggedInWithAValidSession(String username) {
        if(fishermanRepository.findByUsername(username) == null){
            fishermanService.register(username,"test","test","test");
        }
        Fisherman fisherman = fishermanRepository.findByUsername(username);
        Assertions.assertNotNull(fisherman, "User should exist");
        UUID sessionId = fishermanService.signIn(username, "test");

        currentUsername = username;
        currentSessionId = sessionId;
    }

    // Méthode utilitaire pour obtenir le session token (utilisé par d'autres steps)
    public UUID getCurrentSessionId() {
        return currentSessionId;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}
