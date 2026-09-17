package com.safestep.platform.commerce.domain.repositories;

import com.safestep.platform.commerce.domain.model.aggregates.RedeemedCoupon;
import java.util.*;

public interface RedeemedCouponRepository {
    Optional<RedeemedCoupon> findByExternalId(String id);
    List<RedeemedCoupon> findByUsername(String username);
    RedeemedCoupon save(RedeemedCoupon value);
    boolean existsByExternalId(String id);
}
