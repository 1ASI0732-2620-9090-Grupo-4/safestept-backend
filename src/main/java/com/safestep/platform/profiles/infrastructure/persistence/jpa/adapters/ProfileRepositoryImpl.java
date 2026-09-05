package com.safestep.platform.profiles.infrastructure.persistence.jpa.adapters;

import com.safestep.platform.profiles.domain.model.aggregates.Profile;
import com.safestep.platform.profiles.domain.model.events.ProfileCreatedEvent;
import com.safestep.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.safestep.platform.profiles.domain.repositories.ProfileRepository;
import com.safestep.platform.profiles.infrastructure.persistence.jpa.assemblers.ProfilePersistenceAssembler;
import com.safestep.platform.profiles.infrastructure.persistence.jpa.converters.EmailAddressValue;
import com.safestep.platform.profiles.infrastructure.persistence.jpa.repositories.ProfilePersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProfileRepositoryImpl implements ProfileRepository {

    private final ProfilePersistenceRepository profilePersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProfileRepositoryImpl(ProfilePersistenceRepository profilePersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.profilePersistenceRepository = profilePersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Profile> findById(Long id) {
        return profilePersistenceRepository.findById(id).map(ProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Profile> findByEmailAddress(EmailAddress emailAddress) {
        return profilePersistenceRepository.findByEmailAddress(new EmailAddressValue(emailAddress.address()))
                .map(ProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Profile> findAll() {
        return profilePersistenceRepository.findAll().stream().map(ProfilePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Profile save(Profile profile) {
        boolean isNew = profile.getId() == null;
        var savedEntity = profilePersistenceRepository
                .save(ProfilePersistenceAssembler.toPersistenceFromDomain(profile));
        var savedProfile = ProfilePersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedProfile.onCreated();
            savedProfile.domainEvents().forEach(eventPublisher::publishEvent);
            savedProfile.clearDomainEvents();
        }
        return savedProfile;
    }

    @Override
    public boolean existsByEmailAddress(EmailAddress emailAddress) {
        return profilePersistenceRepository.countByEmailAddress(new EmailAddressValue(emailAddress.address())) > 0;
    }
}
