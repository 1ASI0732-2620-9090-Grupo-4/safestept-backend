package com.safestep.platform.profiles.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EmailAddressPersistenceConverter implements AttributeConverter<EmailAddressValue, String> {

    @Override
    public String convertToDatabaseColumn(EmailAddressValue attribute) {
        return attribute == null ? null : attribute.address();
    }

    @Override
    public EmailAddressValue convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new EmailAddressValue(dbData);
    }
}
