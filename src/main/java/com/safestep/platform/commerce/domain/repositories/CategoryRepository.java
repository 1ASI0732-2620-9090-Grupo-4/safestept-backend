package com.safestep.platform.commerce.domain.repositories;

import com.safestep.platform.commerce.domain.model.valueobjects.Category;
import java.util.*;

public interface CategoryRepository {
    List<Category> findAll();
    void save(Category category);
    boolean existsByExternalId(String id);
}
