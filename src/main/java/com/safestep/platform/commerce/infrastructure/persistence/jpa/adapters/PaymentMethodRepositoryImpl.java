package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.valueobjects.PaymentMethod;
import com.safestep.platform.commerce.domain.repositories.PaymentMethodRepository;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.PaymentMethodPersistenceEntity;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories.PaymentMethodPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class PaymentMethodRepositoryImpl implements PaymentMethodRepository {
    private final PaymentMethodPersistenceRepository r;

    public PaymentMethodRepositoryImpl(PaymentMethodPersistenceRepository r) { this.r = r; }

    public List<PaymentMethod> findAll() {
        return r.findAll().stream().map(e -> new PaymentMethod(e.getId(), e.getExternalId(), e.getType(),
                e.getLabel(), e.getDescription(), e.getProcessingFee(), e.isAvailable())).toList();
    }

    public void save(PaymentMethod v) {
        var e = new PaymentMethodPersistenceEntity();
        e.setExternalId(v.externalId());
        e.setType(v.type());
        e.setLabel(v.label());
        e.setDescription(v.description());
        e.setProcessingFee(v.processingFee());
        e.setAvailable(v.available());
        r.save(e);
    }

    public boolean existsByExternalId(String id) { return r.existsByExternalId(id); }
}
