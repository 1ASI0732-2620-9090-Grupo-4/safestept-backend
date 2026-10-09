package com.safestep.platform.profiles.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.safestep.platform.profiles.domain.model.aggregates.Profile;
import com.safestep.platform.profiles.domain.model.queries.GetAllProfilesQuery;
import com.safestep.platform.profiles.domain.model.queries.GetProfileByEmailQuery;
import com.safestep.platform.profiles.domain.model.queries.GetProfileByIdQuery;
import com.safestep.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.safestep.platform.profiles.domain.repositories.ProfileRepository;
import com.safestep.platform.profiles.application.acl.ProfilesContextFacadeImpl;
import com.safestep.platform.profiles.application.commandservices.ProfileCommandService;
import com.safestep.platform.profiles.application.queryservices.ProfileQueryService;
import com.safestep.platform.profiles.domain.model.commands.CreateProfileCommand;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfileQueryServiceImplTest {

    private ProfileRepository repository;
    private ProfileQueryServiceImpl service;
    private final Profile profile = new Profile(4L,
            new com.safestep.platform.profiles.domain.model.valueobjects.PersonName("Ana", "Torres"),
            new EmailAddress("ana@safestep.pe"),
            new com.safestep.platform.profiles.domain.model.valueobjects.StreetAddress("Calle", "1", "Lima", "1", "Peru"));

    @BeforeEach
    void setUp() {
        repository = mock(ProfileRepository.class);
        service = new ProfileQueryServiceImpl(repository);
    }

    @Test
    @DisplayName("queries should delegate to the profile repository (AAA)")
    void queries_DelegateToRepository() {
        // Arrange
        when(repository.findById(4L)).thenReturn(Optional.of(profile));
        when(repository.findByEmailAddress(new EmailAddress("ana@safestep.pe"))).thenReturn(Optional.of(profile));
        when(repository.findAll()).thenReturn(List.of(profile));

        // Act + Assert
        assertSame(profile, service.handle(new GetProfileByIdQuery(4L)).orElseThrow());
        assertSame(profile,
                service.handle(new GetProfileByEmailQuery(new EmailAddress("ana@safestep.pe"))).orElseThrow());
        assertEquals(1, service.handle(new GetAllProfilesQuery()).size());
    }

    @Test
    @DisplayName("ProfilesContextFacadeImpl should expose profile ids to other contexts (AAA)")
    void facade_ExposesProfileIds() {
        // Arrange
        var commands = mock(ProfileCommandService.class);
        var queries = mock(ProfileQueryService.class);
        var facade = new ProfilesContextFacadeImpl(commands, queries);
        when(commands.handle(org.mockito.ArgumentMatchers.any(CreateProfileCommand.class)))
                .thenReturn(Result.success(profile), Result.failure(ApplicationError.conflict("Profile", "dup")));
        when(queries.handle(new GetProfileByEmailQuery(new EmailAddress("ana@safestep.pe"))))
                .thenReturn(Optional.of(profile));
        when(queries.handle(new GetProfileByEmailQuery(new EmailAddress("ghost@safestep.pe"))))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertEquals(4L, facade.createProfile("Ana", "Torres", "ana@safestep.pe", "Calle", "1", "Lima", "1", "Peru"));
        assertEquals(0L, facade.createProfile("Ana", "Torres", "ana@safestep.pe", "Calle", "1", "Lima", "1", "Peru"));
        assertEquals(4L, facade.fetchProfileIdByEmail("ana@safestep.pe"));
        assertEquals(0L, facade.fetchProfileIdByEmail("ghost@safestep.pe"));
        assertTrue(facade.fetchProfileIdByEmail("ghost@safestep.pe") == 0L);
    }
}
