package com.safestep.platform.commerce.domain.repositories;

import com.safestep.platform.commerce.domain.model.entities.CartItem;
import java.util.*;

public interface ShoppingCartRepository {
    List<CartItem> findByUsername(String username);

    Optional<CartItem> findByExternalIdAndUsername(String id, String username);

    CartItem save(CartItem item);

    void delete(CartItem item);

    void deleteByUsername(String username);

    boolean existsByExternalId(String id);
}
