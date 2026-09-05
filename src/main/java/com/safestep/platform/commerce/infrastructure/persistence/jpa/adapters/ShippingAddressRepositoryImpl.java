package com.safestep.platform.commerce.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.commerce.domain.model.aggregates.ShippingAddress;
import com.safestep.platform.commerce.domain.repositories.ShippingAddressRepository;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.entities.ShippingAddressPersistenceEntity;
import com.safestep.platform.commerce.infrastructure.persistence.jpa.repositories.ShippingAddressPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class ShippingAddressRepositoryImpl implements ShippingAddressRepository {
    private final ShippingAddressPersistenceRepository r;

    public ShippingAddressRepositoryImpl(ShippingAddressPersistenceRepository r) { this.r = r; }

    public List<ShippingAddress> findByUsername(String u) {
        return r.findByUsername(u).stream()
                .map(e -> new ShippingAddress(e.getId(), e.getExternalId(), e.getUsername(), e.getLabel(),
                        e.getRecipientName(), e.getPhone(), e.getCountry(), e.getRegion(), e.getCity(), e.getDistrict(),
                        e.getPostalCode(), e.getStreet(), e.getStreetNumber(), e.getReference(), e.isDefaultAddress()))
                .toList();
    }

    public void save(ShippingAddress v) {
        var e = new ShippingAddressPersistenceEntity();
        e.setExternalId(v.getExternalId());
        e.setUsername(v.getUsername());
        e.setLabel(v.getLabel());
        e.setRecipientName(v.getRecipientName());
        e.setPhone(v.getPhone());
        e.setCountry(v.getCountry());
        e.setRegion(v.getRegion());
        e.setCity(v.getCity());
        e.setDistrict(v.getDistrict());
        e.setPostalCode(v.getPostalCode());
        e.setStreet(v.getStreet());
        e.setStreetNumber(v.getStreetNumber());
        e.setReference(v.getReference());
        e.setDefaultAddress(v.isDefaultAddress());
        r.save(e);
    }

    public boolean existsByExternalId(String id) { return r.existsByExternalId(id); }
}
