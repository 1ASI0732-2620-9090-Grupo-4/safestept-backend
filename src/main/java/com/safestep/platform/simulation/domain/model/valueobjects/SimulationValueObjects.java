package com.safestep.platform.simulation.domain.model.valueobjects;

public final class SimulationValueObjects {
    private SimulationValueObjects() {
    }

    public record SimulationSlug(String value) {
        public SimulationSlug {
            if (value == null || value.isBlank())
                throw new IllegalArgumentException("Simulation slug is required");
        }
    }

    public record SimulationReward(int xp, int coins) {
        public SimulationReward {
            if (xp < 0 || coins < 0)
                throw new IllegalArgumentException("Rewards cannot be negative");
        }
    }

    public record Score(int value) {
        public Score {
            if (value < 0 || value > 100)
                throw new IllegalArgumentException("Score must be between 0 and 100");
        }
    }

    public enum Difficulty {
        BASIC, INTERMEDIATE, ADVANCED;

        public static Difficulty from(String value) {
            if (value == null)
                return BASIC;
            var normalized = value.toUpperCase().replace('Á', 'A').replace('É', 'E');
            if (normalized.contains("INTER"))
                return INTERMEDIATE;
            if (normalized.contains("AVAN") || normalized.contains("ADV"))
                return ADVANCED;
            return BASIC;
        }
    }

    public enum AttemptMode {
        PRACTICE, EVALUATION, GUIDED;

        public static AttemptMode from(String value) {
            if (value == null)
                return PRACTICE;
            try {
                return valueOf(value.toUpperCase());
            } catch (Exception ignored) {
                return PRACTICE;
            }
        }
    }

    public enum AttemptStatus {
        IN_PROGRESS, COMPLETED
    }
}
