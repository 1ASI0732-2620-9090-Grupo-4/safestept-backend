package com.safestep.acceptance;

import com.safestep.acceptance.support.AcceptanceSupportConfiguration;
import com.safestep.platform.SafeStepPlatformApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Boots the real application once for the whole Cucumber run. It uses its own in-memory database so the acceptance
 * scenarios never interfere with the other Spring integration tests of the project.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = SafeStepPlatformApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:safestep_bdd;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
@Import(AcceptanceSupportConfiguration.class)
public class CucumberSpringConfiguration {
}
