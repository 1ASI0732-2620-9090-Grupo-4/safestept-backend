package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.entities.CartItem;
import com.safestep.platform.commerce.domain.repositories.ShoppingCartRepository;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.CartItemPersistenceEntity;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories.CartItemPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class ShoppingCartRepositoryImpl implements ShoppingCartRepository {
    private final CartItemPersistenceRepository r;

    public ShoppingCartRepositoryImpl(CartItemPersistenceRepository r) {
        this.r = r;
    }

    private CartItem domain(CartItemPersistenceEntity e) {
        return new CartItem(e.getId(), e.getExternalId(), e.getUsername(), e.getProductId(), e.getQuantity(),
                e.getAddedAt());
    }

    private CartItemPersistenceEntity entity(CartItem d) {
        var e = new CartItemPersistenceEntity();
        e.setId(d.getId());
        e.setExternalId(d.getExternalId());
        e.setUsername(d.getUsername());
        e.setProductId(d.getProductId());
        e.setQuantity(d.getQuantity());
        e.setAddedAt(d.getAddedAt());
        return e;
    }

    public List<CartItem> findByUsername(String u) {
        return r.findByUsername(u).stream().map(this::domain).toList();
    }

    public Optional<CartItem> findByExternalIdAndUsername(String id, String u) {
        return r.findByExternalIdAndUsername(id, u).map(this::domain);
    }

    public CartItem save(CartItem d) {
        var s = r.save(entity(d));
        d.setId(s.getId());
        return d;
    }

    public void delete(CartItem d) {
        r.delete(entity(d));
    }

    public void deleteByUsername(String u) {
        r.deleteByUsername(u);
    }

    public boolean existsByExternalId(String id) {
        return r.existsByExternalId(id);
    }
}
