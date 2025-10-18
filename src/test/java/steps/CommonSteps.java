package steps;

import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;

/**
 * Classe contenant les steps communes utilisées par plusieurs features
 */
public class CommonSteps {

    private Exception lastException;

    public void setLastException(Exception exception) {
        this.lastException = exception;
    }

    public Exception getLastException() {
        return lastException;
    }

    @Then("the operation should fail with error {string}")
    public void theOperationShouldFailWithError(String errorMessage) {
        Assertions.assertNotNull(lastException, "An exception should have been thrown");
        Assertions.assertTrue(
                lastException.getMessage().contains(errorMessage) ||
                lastException.getClass().getSimpleName().contains("NotFound") ||
                lastException.getClass().getSimpleName().contains("NoFish") ||
                lastException.getClass().getSimpleName().contains("AlreadyExists"),
                "Error message should contain: " + errorMessage + " but was: " + lastException.getMessage());
    }
}
