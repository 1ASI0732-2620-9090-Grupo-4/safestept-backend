package com.safestep.platform.simulation.interfaces.rest.resources;

import java.util.List;

public record SimulationResource(String id, String title, String emergencyType, String difficulty, int durationMinutes,
        int xpReward, String imageUrl, String status, int completion, String description, List<String> learningGoals,
        List<StepResource> steps, List<ProductSuggestionResource> productSuggestions, int coinReward) {
}
