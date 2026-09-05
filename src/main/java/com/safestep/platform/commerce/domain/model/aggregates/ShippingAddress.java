package com.safestep.platform.commerce.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

public class ShippingAddress extends AbstractDomainAggregateRoot<ShippingAddress> {
    private Long id;
    private String externalId;
    private String username;
    private String label;
    private String recipientName;
    private String phone;
    private String country;
    private String region;
    private String city;
    private String district;
    private String postalCode;
    private String street;
    private String streetNumber;
    private String reference;
    private boolean defaultAddress;

    public ShippingAddress() {}

    public ShippingAddress(Long id, String externalId, String username, String label, String recipientName,
            String phone, String country, String region, String city, String district, String postalCode, String street,
            String streetNumber, String reference, boolean defaultAddress) {
        this.id = id;
        this.externalId = externalId;
        this.username = username;
        this.label = label;
        this.recipientName = recipientName;
        this.phone = phone;
        this.country = country;
        this.region = region;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.street = street;
        this.streetNumber = streetNumber;
        this.reference = reference;
        this.defaultAddress = defaultAddress;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public String getUsername() { return username; }
    public String getLabel() { return label; }
    public String getRecipientName() { return recipientName; }
    public String getPhone() { return phone; }
    public String getCountry() { return country; }
    public String getRegion() { return region; }
    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getPostalCode() { return postalCode; }
    public String getStreet() { return street; }
    public String getStreetNumber() { return streetNumber; }
    public String getReference() { return reference; }
    public boolean isDefaultAddress() { return defaultAddress; }
}
