package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.valueobjects.ProductRecommendation;
import com.safestep.platform.commerce.domain.repositories.ProductRecommendationRepository;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.ProductRecommendationPersistenceEntity;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories.ProductRecommendationPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class ProductRecommendationRepositoryImpl implements ProductRecommendationRepository {
    private final ProductRecommendationPersistenceRepository r;

    public ProductRecommendationRepositoryImpl(ProductRecommendationPersistenceRepository r) { this.r = r; }

    public List<ProductRecommendation> findByUsername(String u) {
        return r.findByUsername(u).stream()
                .map(e -> new ProductRecommendation(e.getId(), e.getExternalId(), e.getUsername(),
                        e.getProductId(), e.getReason(), e.getRelatedSimulationId(), e.getPriority(), e.isDismissed()))
                .toList();
    }

    public List<ProductRecommendation> findAll() {
        return r.findAll().stream()
                .map(e -> new ProductRecommendation(e.getId(), e.getExternalId(), e.getUsername(),
                        e.getProductId(), e.getReason(), e.getRelatedSimulationId(), e.getPriority(), e.isDismissed()))
                .toList();
    }

    public void save(ProductRecommendation v) {
        var e = new ProductRecommendationPersistenceEntity();
        e.setExternalId(v.externalId());
        e.setUsername(v.username());
        e.setProductId(v.productId());
        e.setReason(v.reason());
        e.setRelatedSimulationId(v.relatedSimulationId());
        e.setPriority(v.priority());
        e.setDismissed(v.dismissed());
        r.save(e);
    }

    public boolean existsByExternalId(String id) { return r.existsByExternalId(id); }
}
