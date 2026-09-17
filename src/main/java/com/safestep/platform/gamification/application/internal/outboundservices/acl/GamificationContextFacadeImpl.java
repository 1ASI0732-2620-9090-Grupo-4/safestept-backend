package com.safestep.platform.gamification.application.internal.outboundservices.acl;

import com.safestep.platform.gamification.domain.model.aggregates.CoinSpend;
import com.safestep.platform.gamification.domain.repositories.CoinSpendRepository;
import com.safestep.platform.gamification.domain.repositories.PlayerProgressRepository;
import com.safestep.platform.gamification.interfaces.acl.GamificationContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class GamificationContextFacadeImpl implements GamificationContextFacade {
    private final PlayerProgressRepository repository;
    private final CoinSpendRepository coinSpends;

    public GamificationContextFacadeImpl(PlayerProgressRepository r, CoinSpendRepository coinSpends) {
        repository = r;
        this.coinSpends = coinSpends;
    }

    public ProgressSnapshot progressByUsername(String u) {
        return repository.findByUsername(u).map(p -> new ProgressSnapshot(p.getLevel(), p.getXp(), p.getSafeCoins(),
                p.getStreakDays(), p.getCompletedSimulations())).orElse(new ProgressSnapshot(1, 0, 0, 0, 0));
    }

    @Transactional
    public boolean spendCoins(String username, int amount, String couponId, String couponTitle) {
        var progress = repository.findByUsername(username);
        if (progress.isEmpty())
            return false;
        try {
            progress.get().spendCoins(amount);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return false;
        }
        repository.save(progress.get());
        coinSpends.save(new CoinSpend(null, "spend-" + UUID.randomUUID(), username, couponId, couponTitle, amount,
                Instant.now()));
        return true;
    }
}
