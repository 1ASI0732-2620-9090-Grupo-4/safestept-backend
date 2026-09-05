package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.aggregates.Product;
import com.safestep.platform.commerce.domain.repositories.ProductRepository;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.ProductPersistenceEntity;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories.ProductPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductPersistenceRepository r;

    public ProductRepositoryImpl(ProductPersistenceRepository r) {
        this.r = r;
    }

    private Product domain(ProductPersistenceEntity e) {
        return new Product(e.getId(), e.getExternalId(), e.getName(), e.getCategory(), e.getType(), e.getPrice(),
                e.getOldPrice(), e.getRating(), e.getStock(), e.getImageUrl(), e.getDescription(), e.getTags());
    }

    private ProductPersistenceEntity entity(Product d) {
        var e = new ProductPersistenceEntity();
        e.setId(d.getId());
        e.setExternalId(d.getExternalId());
        e.setName(d.getName());
        e.setCategory(d.getCategory());
        e.setType(d.getType());
        e.setPrice(d.getPrice());
        e.setOldPrice(d.getOldPrice());
        e.setRating(d.getRating());
        e.setStock(d.getStock());
        e.setImageUrl(d.getImageUrl());
        e.setDescription(d.getDescription());
        e.setTags(new ArrayList<>(d.getTags()));
        return e;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return r.findAll().stream().map(this::domain).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findByExternalId(String id) {
        return r.findByExternalId(id).map(this::domain);
    }

    @Transactional
    public Product save(Product d) {
        var s = r.save(entity(d));
        d.setId(s.getId());
        return d;
    }

    public boolean existsByExternalId(String id) {
        return r.existsByExternalId(id);
    }

    @Transactional
    public void delete(Product product) {
        r.delete(entity(product));
    }
}
