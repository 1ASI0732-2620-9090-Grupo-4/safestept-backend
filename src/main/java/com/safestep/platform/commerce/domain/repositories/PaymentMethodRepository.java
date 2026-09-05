package com.safestep.platform.commerce.domain.repositories;

import com.safestep.platform.commerce.domain.model.valueobjects.PaymentMethod;
import java.util.*;

public interface PaymentMethodRepository {
    List<PaymentMethod> findAll();
    void save(PaymentMethod method);
    boolean existsByExternalId(String id);
}
