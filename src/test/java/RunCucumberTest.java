import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

@RunWith(Cucumber.class)
@SpringBootTest(classes = com.ggc.fishingcopilot.DemoApplication.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "steps"
)
public class RunCucumberTest {


    @ClassRule
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2")
            .withDatabaseName("fishing-copilot")
            .withUsername("test")
            .withPassword("test")
            .withExposedPorts(5432);

    static {
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }
}