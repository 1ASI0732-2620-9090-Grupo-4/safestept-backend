package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import com.safestep.platform.commerce.domain.repositories.CouponRepository;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.CouponPersistenceEntity;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories.CouponPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class CouponRepositoryImpl implements CouponRepository {
    private final CouponPersistenceRepository r;

    public CouponRepositoryImpl(CouponPersistenceRepository r) { this.r = r; }

    public List<Coupon> findAll() {
        return r.findAll().stream()
                .map(e -> new Coupon(e.getId(), e.getExternalId(), e.getTitle(), e.getCostCoins(), e.getDiscount()))
                .toList();
    }

    public Optional<Coupon> findByExternalId(String id) {
        return r.findByExternalId(id)
                .map(e -> new Coupon(e.getId(), e.getExternalId(), e.getTitle(), e.getCostCoins(), e.getDiscount()));
    }

    public Coupon save(Coupon v) {
        var e = new CouponPersistenceEntity();
        e.setId(v.getId());
        e.setExternalId(v.getExternalId());
        e.setTitle(v.getTitle());
        e.setCostCoins(v.getCostCoins());
        e.setDiscount(v.getDiscount());
        var s = r.save(e);
        v.setId(s.getId());
        return v;
    }

    public boolean existsByExternalId(String id) { return r.existsByExternalId(id); }

    public void delete(Coupon coupon) {
        r.deleteById(coupon.getId());
    }
}
