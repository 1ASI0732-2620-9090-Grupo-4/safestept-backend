package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.aggregates.RedeemedCoupon;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.CouponType;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.RedemptionStatus;
import com.safestep.platform.commerce.domain.repositories.RedeemedCouponRepository;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.RedeemedCouponPersistenceEntity;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories.RedeemedCouponPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class RedeemedCouponRepositoryImpl implements RedeemedCouponRepository {
    private final RedeemedCouponPersistenceRepository r;

    public RedeemedCouponRepositoryImpl(RedeemedCouponPersistenceRepository r) { this.r = r; }

    public Optional<RedeemedCoupon> findByExternalId(String id) {
        return r.findByExternalId(id).map(this::toDomain);
    }

    public List<RedeemedCoupon> findByUsername(String username) {
        return r.findByUsernameOrderByRedeemedAtDesc(username).stream().map(this::toDomain).toList();
    }

    public RedeemedCoupon save(RedeemedCoupon v) {
        var e = new RedeemedCouponPersistenceEntity();
        e.setId(v.getId());
        e.setExternalId(v.getExternalId());
        e.setUsername(v.getUsername());
        e.setCouponId(v.getCouponId());
        e.setTitle(v.getTitle());
        e.setType(v.getType().name());
        e.setDiscountPercentage(v.getDiscountPercentage());
        e.setMinPurchaseAmount(v.getMinPurchaseAmount());
        e.setRedeemedAt(v.getRedeemedAt());
        e.setUsedAt(v.getUsedAt());
        e.setStatus(v.getStatus().name());
        var s = r.save(e);
        v.setId(s.getId());
        return v;
    }

    private RedeemedCoupon toDomain(RedeemedCouponPersistenceEntity e) {
        return new RedeemedCoupon(e.getId(), e.getExternalId(), e.getUsername(), e.getCouponId(), e.getTitle(),
                CouponType.from(e.getType()), e.getDiscountPercentage(), e.getMinPurchaseAmount(), e.getRedeemedAt(),
                e.getUsedAt(), RedemptionStatus.from(e.getStatus()));
    }

    public boolean existsByExternalId(String id) { return r.existsByExternalId(id); }
}
