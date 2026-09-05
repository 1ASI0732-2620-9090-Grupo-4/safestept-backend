package com.safestep.platform.commerce.infrastructure.persistence.jpa.entities;

import com.safestep.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "shipping_addresses")
public class ShippingAddressPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(unique = true, nullable = false)
    private String externalId;
    private String username, label, recipientName, phone, country, region, city, district, postalCode, street,
            streetNumber, reference;
    private boolean defaultAddress;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String v) {
        externalId = v;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        username = v;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String v) {
        label = v;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String v) {
        recipientName = v;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String v) {
        phone = v;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String v) {
        country = v;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String v) {
        region = v;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String v) {
        city = v;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String v) {
        district = v;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String v) {
        postalCode = v;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String v) {
        street = v;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String v) {
        streetNumber = v;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String v) {
        reference = v;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(boolean v) {
        defaultAddress = v;
    }
}
