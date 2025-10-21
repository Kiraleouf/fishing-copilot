import io.cucumber.core.cli.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CucumberRunnerTest {

  @Test
  void runCucumberFeatures() {
    // Exécution du moteur Cucumber via la CLI interne
    byte exitStatus = Main.run(new String[]{
      "--glue", "steps",
      "--plugin", "pretty",
      "--plugin", "summary",
      "--plugin", "junit:target/cucumber-reports/Cucumber.xml",
      "classpath:features"
    }, Thread.currentThread().getContextClassLoader());

    // Si un scénario échoue, exitStatus != 0 → le test JUnit échoue
    Assertions.assertEquals(0, exitStatus, "Some Cucumber scenarios failed. See report for details.");
  }
}
