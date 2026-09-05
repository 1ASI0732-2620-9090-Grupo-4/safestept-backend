package com.safestep.platform.profiles.infrastructure.persistence.jpa.repositories;

import com.safestep.platform.profiles.infrastructure.persistence.jpa.converters.EmailAddressValue;
import com.safestep.platform.profiles.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilePersistenceRepository extends JpaRepository<ProfilePersistenceEntity, Long> {

    @Query("select profile from ProfilePersistenceEntity profile where profile.emailAddress = :emailAddress")
    Optional<ProfilePersistenceEntity> findByEmailAddress(@Param("emailAddress") EmailAddressValue emailAddress);

    @Query("select count(profile) from ProfilePersistenceEntity profile where profile.emailAddress = :emailAddress")
    long countByEmailAddress(@Param("emailAddress") EmailAddressValue emailAddress);
}
