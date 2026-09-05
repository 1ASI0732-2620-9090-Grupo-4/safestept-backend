package com.safestep.platform.profiles.infrastructure.persistence.jpa.assemblers;

import com.safestep.platform.profiles.domain.model.aggregates.Profile;
import com.safestep.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.safestep.platform.profiles.domain.model.valueobjects.PersonName;
import com.safestep.platform.profiles.domain.model.valueobjects.StreetAddress;
import com.safestep.platform.profiles.infrastructure.persistence.jpa.converters.EmailAddressValue;
import com.safestep.platform.profiles.infrastructure.persistence.jpa.embeddables.PersonNamePersistenceEmbeddable;
import com.safestep.platform.profiles.infrastructure.persistence.jpa.embeddables.StreetAddressPersistenceEmbeddable;
import com.safestep.platform.profiles.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;

public final class ProfilePersistenceAssembler {

    private ProfilePersistenceAssembler() {
    }

    public static Profile toDomainFromPersistence(ProfilePersistenceEntity entity) {
        return new Profile(entity.getId(), toDomainFromPersistence(entity.getName()),
                new EmailAddress(entity.getEmailAddress().address()),
                toDomainFromPersistence(entity.getStreetAddress()));
    }

    public static ProfilePersistenceEntity toPersistenceFromDomain(Profile profile) {
        var entity = new ProfilePersistenceEntity();
        entity.setId(profile.getId());
        entity.setName(toPersistenceFromDomain(profile.getName()));
        entity.setEmailAddress(new EmailAddressValue(profile.getEmailAddressValue().address()));
        entity.setStreetAddress(toPersistenceFromDomain(profile.getStreetAddressValue()));
        return entity;
    }

    private static PersonName toDomainFromPersistence(PersonNamePersistenceEmbeddable value) {
        return value == null ? null : new PersonName(value.getFirstName(), value.getLastName());
    }

    private static StreetAddress toDomainFromPersistence(StreetAddressPersistenceEmbeddable value) {
        return value == null ? null
                : new StreetAddress(value.getStreet(), value.getNumber(), value.getCity(), value.getPostalCode(),
                        value.getCountry());
    }

    private static PersonNamePersistenceEmbeddable toPersistenceFromDomain(PersonName value) {
        return value == null ? null : new PersonNamePersistenceEmbeddable(value.firstName(), value.lastName());
    }

    private static StreetAddressPersistenceEmbeddable toPersistenceFromDomain(StreetAddress value) {
        return value == null ? null
                : new StreetAddressPersistenceEmbeddable(value.street(), value.number(), value.city(),
                        value.postalCode(), value.country());
    }
}
