package com.safestep.acceptance.support;

import com.safestep.platform.gamification.domain.repositories.PlayerProgressRepository;
import io.cucumber.spring.ScenarioScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AcceptanceSupportConfiguration {

    @Bean
    public ApiClient apiClient(Environment environment) {
        return new ApiClient(environment);
    }

    @Bean
    public PlayerFactory playerFactory(ApiClient apiClient, PlayerProgressRepository progress) {
        return new PlayerFactory(apiClient, progress);
    }

    @Bean
    @ScenarioScope
    public ScenarioContext scenarioContext() {
        return new ScenarioContext();
    }
}
