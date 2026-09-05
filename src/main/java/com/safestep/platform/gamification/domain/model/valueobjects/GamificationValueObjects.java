package com.safestep.platform.gamification.domain.model.valueobjects;

public final class GamificationValueObjects {
    private GamificationValueObjects() {
    }

    public record ExperiencePoints(int value) {
        public ExperiencePoints {
            if (value < 0)
                throw new IllegalArgumentException("XP cannot be negative");
        }
    }

    public record SafeCoins(int value) {
        public SafeCoins {
            if (value < 0)
                throw new IllegalArgumentException("Coins cannot be negative");
        }
    }

    public record PlayerLevel(int value) {
        public PlayerLevel {
            if (value < 1)
                throw new IllegalArgumentException("Level must be positive");
        }
    }

    public enum MissionCadence {
        DAILY, WEEKLY, MONTHLY;

        public static MissionCadence from(String v) {
            if (v == null)
                return DAILY;
            var n = v.toUpperCase();
            if (n.contains("SEMAN") || n.contains("WEEK"))
                return WEEKLY;
            if (n.contains("MENS") || n.contains("MONTH"))
                return MONTHLY;
            return DAILY;
        }
    }

    public enum BadgeRarity {
        COMMON, RARE, EPIC, LEGENDARY;

        public static BadgeRarity from(String v) {
            if (v == null)
                return COMMON;
            var n = v.toUpperCase();
            if (n.contains("RAR"))
                return RARE;
            if (n.contains("EP"))
                return EPIC;
            if (n.contains("LEG"))
                return LEGENDARY;
            return COMMON;
        }
    }
}
