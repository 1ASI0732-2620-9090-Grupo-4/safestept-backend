package com.safestep.platform.simulation.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.safestep.platform.simulation.domain.model.entities.SimulationStep;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.Difficulty;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.SimulationReward;
import com.safestep.platform.simulation.domain.model.valueobjects.SimulationValueObjects.SimulationSlug;

import java.util.List;

public class MedicalSimulation extends AbstractDomainAggregateRoot<MedicalSimulation> {
    private Long id;
    private final SimulationSlug slug;
    private final String title;
    private final String emergencyType;
    private final Difficulty difficulty;
    private final int durationMinutes;
    private final String description;
    private final String imageUrl;
    private final String status;
    private final int completion;
    private final SimulationReward reward;
    private final List<String> learningGoals;
    private final List<SimulationStep> steps;
    private final List<ProductSuggestion> productSuggestions;

    public MedicalSimulation(Long id, String slug, String title, String emergencyType, Difficulty difficulty,
            int durationMinutes, String description, String imageUrl, String status, int completion,
            SimulationReward reward, List<String> learningGoals, List<SimulationStep> steps,
            List<ProductSuggestion> productSuggestions) {
        this.id = id;
        this.slug = new SimulationSlug(slug);
        this.title = title;
        this.emergencyType = emergencyType;
        this.difficulty = difficulty;
        this.durationMinutes = durationMinutes;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.completion = completion;
        this.reward = reward;
        this.learningGoals = learningGoals == null ? List.of() : List.copyOf(learningGoals);
        this.steps = steps == null ? List.of() : List.copyOf(steps);
        this.productSuggestions = productSuggestions == null ? List.of() : List.copyOf(productSuggestions);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlug() {
        return slug.value();
    }

    public String getTitle() {
        return title;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public int getCompletion() {
        return completion;
    }

    public SimulationReward getReward() {
        return reward;
    }

    public List<String> getLearningGoals() {
        return learningGoals;
    }

    public List<SimulationStep> getSteps() {
        return steps;
    }

    public List<ProductSuggestion> getProductSuggestions() {
        return productSuggestions;
    }

    public record ProductSuggestion(String productId, String reason) {
    }
}
