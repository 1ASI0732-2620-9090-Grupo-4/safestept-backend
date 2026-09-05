package com.safestep.platform.simulation.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "medical_simulations")
public class MedicalSimulationPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true)
    private String slug;
    @Column(nullable = false)
    private String title;
    private String emergencyType;
    private String difficulty;
    private int durationMinutes;
    @Column(length = 2000)
    private String description;
    private String imageUrl;
    private String status;
    private int completion;
    private int xpReward;
    private int coinReward;
    @ElementCollection
    @CollectionTable(name = "simulation_learning_goals", joinColumns = @JoinColumn(name = "simulation_id"))
    @Column(name = "goal")
    private List<String> learningGoals = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "simulation_steps", joinColumns = @JoinColumn(name = "simulation_id"))
    private List<StepEmbeddable> steps = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "simulation_options", joinColumns = @JoinColumn(name = "simulation_id"))
    private List<OptionEmbeddable> options = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "simulation_product_suggestions", joinColumns = @JoinColumn(name = "simulation_id"))
    private List<ProductSuggestionEmbeddable> productSuggestions = new ArrayList<>();

    public String getSlug() {
        return slug;
    }

    public void setSlug(String v) {
        slug = v;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String v) {
        emergencyType = v;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String v) {
        difficulty = v;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int v) {
        durationMinutes = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String v) {
        imageUrl = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public int getCompletion() {
        return completion;
    }

    public void setCompletion(int v) {
        completion = v;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int v) {
        xpReward = v;
    }

    public int getCoinReward() {
        return coinReward;
    }

    public void setCoinReward(int v) {
        coinReward = v;
    }

    public List<String> getLearningGoals() {
        return learningGoals;
    }

    public void setLearningGoals(List<String> v) {
        learningGoals = v;
    }

    public List<StepEmbeddable> getSteps() {
        return steps;
    }

    public void setSteps(List<StepEmbeddable> v) {
        steps = v;
    }

    public List<OptionEmbeddable> getOptions() {
        return options;
    }

    public void setOptions(List<OptionEmbeddable> v) {
        options = v;
    }

    public List<ProductSuggestionEmbeddable> getProductSuggestions() {
        return productSuggestions;
    }

    public void setProductSuggestions(List<ProductSuggestionEmbeddable> v) {
        productSuggestions = v;
    }

    @Embeddable
    public static class StepEmbeddable {
        private String externalId;
        @Column(length = 1000)
        private String prompt;
        private String correctOptionId;

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String v) {
            externalId = v;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String v) {
            prompt = v;
        }

        public String getCorrectOptionId() {
            return correctOptionId;
        }

        public void setCorrectOptionId(String v) {
            correctOptionId = v;
        }
    }

    @Embeddable
    public static class OptionEmbeddable {
        private String stepExternalId;
        private String externalId;
        @Column(length = 1000)
        private String label;
        @Column(length = 1000)
        private String feedback;

        public String getStepExternalId() {
            return stepExternalId;
        }

        public void setStepExternalId(String v) {
            stepExternalId = v;
        }

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String v) {
            externalId = v;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String v) {
            label = v;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String v) {
            feedback = v;
        }
    }

    @Embeddable
    public static class ProductSuggestionEmbeddable {
        private String productId;
        @Column(length = 1000)
        private String reason;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String v) {
            productId = v;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String v) {
            reason = v;
        }
    }
}
