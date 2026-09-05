package com.safestep.platform.commerce.domain.repositories;

import com.safestep.platform.commerce.domain.model.aggregates.Coupon;
import java.util.*;

public interface CouponRepository {
    List<Coupon> findAll();
    Optional<Coupon> findByExternalId(String id);
    Coupon save(Coupon coupon);
    boolean existsByExternalId(String id);
    void delete(Coupon coupon);
}
