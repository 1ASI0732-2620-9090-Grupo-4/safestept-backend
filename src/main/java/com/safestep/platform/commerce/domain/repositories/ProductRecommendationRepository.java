package com.safestep.platform.commerce.domain.repositories;

import com.safestep.platform.commerce.domain.model.valueobjects.ProductRecommendation;
import java.util.*;

public interface ProductRecommendationRepository {
    List<ProductRecommendation> findByUsername(String username);
    List<ProductRecommendation> findAll();
    void save(ProductRecommendation recommendation);
    boolean existsByExternalId(String id);
}
