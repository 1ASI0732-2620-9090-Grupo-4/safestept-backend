package com.safestep.platform.commerce.domain.repositories;

import com.safestep.platform.commerce.domain.model.aggregates.Product;
import java.util.*;

public interface ProductRepository {
    List<Product> findAll();

    Optional<Product> findByExternalId(String id);

    Product save(Product p);

    boolean existsByExternalId(String id);

    void delete(Product product);
}
