package com.safestep.platform.gamification.application.internal.eventhandlers;

import com.safestep.platform.gamification.domain.model.aggregates.*;
import com.safestep.platform.gamification.domain.repositories.*;
import com.safestep.platform.simulation.interfaces.events.SimulationAttemptCompletedIntegrationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
public class SimulationAttemptCompletedIntegrationEventHandler {
    private final PlayerProgressRepository progress;
    private final CoinTransactionRepository transactions;
    private final PlayerAchievementRepository achievements;
    private final MissionRepository missions;
    private final BadgeRepository badges;

    public SimulationAttemptCompletedIntegrationEventHandler(PlayerProgressRepository p, CoinTransactionRepository t,
            PlayerAchievementRepository a, MissionRepository m, BadgeRepository b) {
        progress = p;
        transactions = t;
        achievements = a;
        missions = m;
        badges = b;
    }

    @EventListener
    @Transactional
    public void on(SimulationAttemptCompletedIntegrationEvent event) {
        String transactionId = "attempt-" + event.attemptId();
        if (transactions.existsByExternalId(transactionId))
            return;
        var player = progress.findByUsername(event.username())
                .orElseGet(() -> new PlayerProgress(null, event.username(), 1, 0, 0, 0, 0, null));
        player.reward(event.xpReward(), event.coinReward());
        progress.save(player);
        transactions.save(new CoinTransaction(null, transactionId, event.username(), event.simulationSlug(),
                event.simulationTitle(), event.coinReward(), event.coinReward(), event.score(), 1.0, event.accuracy(),
                event.occurredOn()));
        missions.findAll().stream().filter(m -> m
                .getCadence() == com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.MissionCadence.DAILY)
                .findFirst()
                .ifPresent(m -> achievements.advanceMission(event.username(), m.getExternalId(), m.getGoal()));
        if (player.getCompletedSimulations() >= 1)
            badges.findAll().stream()
                    .filter(b -> b.getExternalId().contains("rcp") || b.getExternalId().contains("first")).findFirst()
                    .ifPresent(b -> achievements.unlockBadge(event.username(), b.getExternalId()));
    }
}
