package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
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
        return r.findAll().stream().map(this::toDomain).toList();
    }

    public Optional<Coupon> findByExternalId(String id) {
        return r.findByExternalId(id).map(this::toDomain);
    }

    public Coupon save(Coupon v) {
        var e = new CouponPersistenceEntity();
        e.setId(v.getId());
        e.setExternalId(v.getExternalId());
        e.setTitle(v.getTitle());
        e.setCostCoins(v.getCostCoins());
        e.setType(v.getType().name());
        e.setDiscountPercentage(v.getDiscountPercentage());
        e.setMinPurchaseAmount(v.getMinPurchaseAmount());
        var s = r.save(e);
        v.setId(s.getId());
        return v;
    }

    private Coupon toDomain(CouponPersistenceEntity e) {
        return new Coupon(e.getId(), e.getExternalId(), e.getTitle(), e.getCostCoins(), CouponType.from(e.getType()),
                e.getDiscountPercentage(), e.getMinPurchaseAmount());
    }

    public boolean existsByExternalId(String id) { return r.existsByExternalId(id); }

    public void delete(Coupon coupon) {
        r.deleteById(coupon.getId());
    }
}
