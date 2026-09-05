package com.safestep.platform.commerce.domain.repositories;

import com.safestep.platform.commerce.domain.model.aggregates.ShippingAddress;
import java.util.*;

public interface ShippingAddressRepository {
    List<ShippingAddress> findByUsername(String username);
    void save(ShippingAddress address);
    boolean existsByExternalId(String id);
}
