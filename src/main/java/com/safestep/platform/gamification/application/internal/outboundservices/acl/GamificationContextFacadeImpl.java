package com.safestep.platform.gamification.application.internal.outboundservices.acl;

import com.safestep.platform.gamification.domain.repositories.PlayerProgressRepository;
import com.safestep.platform.gamification.interfaces.acl.GamificationContextFacade;
import org.springframework.stereotype.Service;

@Service
public class GamificationContextFacadeImpl implements GamificationContextFacade {
    private final PlayerProgressRepository repository;

    public GamificationContextFacadeImpl(PlayerProgressRepository r) {
        repository = r;
    }

    public ProgressSnapshot progressByUsername(String u) {
        return repository.findByUsername(u).map(p -> new ProgressSnapshot(p.getLevel(), p.getXp(), p.getSafeCoins(),
                p.getStreakDays(), p.getCompletedSimulations())).orElse(new ProgressSnapshot(1, 0, 0, 0, 0));
    }
}
