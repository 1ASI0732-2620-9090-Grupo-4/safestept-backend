package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.valueobjects.Category;
import com.safestep.platform.commerce.domain.repositories.CategoryRepository;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.CategoryPersistenceEntity;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories.CategoryPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {
    private final CategoryPersistenceRepository r;

    public CategoryRepositoryImpl(CategoryPersistenceRepository r) { this.r = r; }

    public List<Category> findAll() {
        return r.findAll().stream()
                .map(e -> new Category(e.getId(), e.getExternalId(), e.getName(), e.getProductCount())).toList();
    }

    public void save(Category v) {
        var e = new CategoryPersistenceEntity();
        e.setExternalId(v.externalId());
        e.setName(v.name());
        e.setProductCount(v.productCount());
        r.save(e);
    }

    public boolean existsByExternalId(String id) { return r.existsByExternalId(id); }
}
