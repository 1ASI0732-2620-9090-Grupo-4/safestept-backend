package com.safestep.apitests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.karatelabs.core.Runner;
import io.karatelabs.core.SuiteResult;
import org.junit.jupiter.api.Test;

/**
 * Runs every Karate feature under {@code com/safestep/apitests} against the API configured through
 * {@code -Dapi.baseUrl=...} (defaults to the isolated test instance on port 8093).
 */
class ApiTestRunner {

    @Test
    void runAllFeatures() {
        SuiteResult result = Runner.path("classpath:com/safestep/apitests")
                .outputHtmlReport(true)
                .parallel(4);

        assertTrue(result.isPassed(), "Karate API scenarios failed, see target/karate-reports");
    }
}
