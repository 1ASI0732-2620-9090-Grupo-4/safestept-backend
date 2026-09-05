package com.safestep.platform.gamification.infrastructure.persistence.jpa.seed;

import com.safestep.platform.gamification.domain.model.aggregates.*;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.*;
import com.safestep.platform.gamification.domain.repositories.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.*;
import java.time.*;

@Component
public class GamificationSeedEventHandler {
    private final ObjectMapper mapper;
    private final MissionRepository missions;
    private final BadgeRepository badges;
    private final CoinTransactionRepository transactions;
    private final PlayerProgressRepository progress;

    public GamificationSeedEventHandler(ObjectMapper m, MissionRepository mr, BadgeRepository br,
            CoinTransactionRepository tr, PlayerProgressRepository pr) {
        mapper = m;
        missions = mr;
        badges = br;
        transactions = tr;
        progress = pr;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() throws Exception {
        try (var in = new ClassPathResource("safestep-seed.json").getInputStream()) {
            JsonNode root = mapper.readTree(in);
            for (JsonNode n : root.path("missions")) {
                String id = n.path("id").asText();
                if (!missions.existsByExternalId(id))
                    missions.save(new Mission(null, id, n.path("title").asText(),
                            MissionCadence.from(n.path("cadence").asText()), n.path("goal").asInt(),
                            n.path("rewardXp").asInt(), n.path("rewardCoins").asInt(), n.path("status").asText(),
                            n.path("instructions").asText(), n.path("unlockRequirement").asText()));
            }
            for (JsonNode n : root.path("badges")) {
                String id = n.path("id").asText();
                if (!badges.existsByExternalId(id))
                    badges.save(
                            new Badge(null, id, n.path("name").asText(), BadgeRarity.from(n.path("rarity").asText()),
                                    n.path("description").asText(), n.path("unlockRequirement").asText()));
            }
            for (JsonNode n : root.path("coinTransactions")) {
                String id = n.path("id").asText();
                if (!transactions.existsByExternalId(id))
                    transactions.save(
                            new CoinTransaction(null, id, n.path("userId").asText(), n.path("simulationId").asText(),
                                    n.path("simulationTitle").asText(), n.path("baseCoins").asInt(),
                                    n.path("earnedCoins").asInt(), 0, n.path("multiplier").asDouble(),
                                    n.path("accuracy").asDouble(), Instant.parse(n.path("createdAt").asText())));
            }
            if (progress.findByUsername("backend.check").isEmpty())
                progress.save(new PlayerProgress(null, "backend.check", 11, 10340, 420, 28, 42,
                        LocalDate.now().minusDays(1)));
            int rank = 0;
            for (JsonNode n : root.path("leaderboard")) {
                String username = "leaderboard-" + (++rank) + "-" + n.path("name").asText();
                if (progress.findByUsername(username).isEmpty())
                    progress.save(new PlayerProgress(null, username, 1 + n.path("xp").asInt() / 1000,
                            n.path("xp").asInt(), 0, n.path("streak").asInt(), 0, LocalDate.now().minusDays(1)));
            }
        }
    }
}
