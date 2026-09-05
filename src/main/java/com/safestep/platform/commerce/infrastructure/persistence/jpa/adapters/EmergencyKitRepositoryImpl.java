package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.valueobjects.EmergencyKit;
import com.safestep.platform.commerce.domain.repositories.EmergencyKitRepository;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.EmergencyKitPersistenceEntity;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories.EmergencyKitPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class EmergencyKitRepositoryImpl implements EmergencyKitRepository {
    private final EmergencyKitPersistenceRepository r;

    public EmergencyKitRepositoryImpl(EmergencyKitPersistenceRepository r) { this.r = r; }

    public List<EmergencyKit> findAll() {
        return r.findAll().stream()
                .map(e -> new EmergencyKit(e.getId(), e.getExternalId(), e.getName(), e.getDescription(), e.getLevel(),
                        e.getIndividualPrice(), e.getKitPrice(), e.getSavings(), e.getSavingsPercentage(),
                        e.getImageUrl(), e.isPopular()))
                .toList();
    }

    public void save(EmergencyKit v) {
        var e = new EmergencyKitPersistenceEntity();
        e.setExternalId(v.externalId());
        e.setName(v.name());
        e.setDescription(v.description());
        e.setLevel(v.level());
        e.setIndividualPrice(v.individualPrice());
        e.setKitPrice(v.kitPrice());
        e.setSavings(v.savings());
        e.setSavingsPercentage(v.savingsPercentage());
        e.setImageUrl(v.imageUrl());
        e.setPopular(v.popular());
        r.save(e);
    }

    public boolean existsByExternalId(String id) { return r.existsByExternalId(id); }
}
