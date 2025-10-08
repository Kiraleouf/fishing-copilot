package steps;// src/test/java/steps/steps.CucumberSpringConfiguration.java
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = com.ggc.fishingcopilot.FishingCopilotApplicationKt.class)
public class CucumberSpringConfiguration { }