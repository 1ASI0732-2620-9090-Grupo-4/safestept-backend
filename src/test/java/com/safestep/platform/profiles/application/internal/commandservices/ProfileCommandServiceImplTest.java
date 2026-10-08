package com.safestep.platform.profiles.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.safestep.platform.profiles.domain.model.aggregates.Profile;
import com.safestep.platform.profiles.domain.model.commands.CreateProfileCommand;
import com.safestep.platform.profiles.domain.model.commands.UpdateProfileCommand;
import com.safestep.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.safestep.platform.profiles.domain.repositories.ProfileRepository;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfileCommandServiceImplTest {

    private ProfileRepository repository;
    private ProfileCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(ProfileRepository.class);
        service = new ProfileCommandServiceImpl(repository);
    }

    private CreateProfileCommand createCommand(String email) {
        return new CreateProfileCommand("Ana", "Torres", email, "Av. Lima", "123", "Lima", "15001", "Peru");
    }

    private ApplicationError errorOf(Result<?, ApplicationError> result) {
        return ((Result.Failure<?, ApplicationError>) result).error();
    }

    @Test
    @DisplayName("handle(CreateProfileCommand) should save a new profile (AAA)")
    void create_NewProfile_IsSaved() {
        // Arrange
        when(repository.existsByEmailAddress(new EmailAddress("ana@safestep.pe"))).thenReturn(false);
        when(repository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = service.handle(createCommand("ana@safestep.pe"));

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Ana Torres", result.toOptional().orElseThrow().getFullName());
    }

    @Test
    @DisplayName("handle(CreateProfileCommand) should report a conflict for a duplicated email (AAA)")
    void create_DuplicatedEmail_ReturnsConflict() {
        // Arrange
        when(repository.existsByEmailAddress(new EmailAddress("ana@safestep.pe"))).thenReturn(true);

        // Act
        var result = service.handle(createCommand("ana@safestep.pe"));

        // Assert
        assertEquals("PROFILE_CONFLICT", errorOf(result).code());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("handle(CreateProfileCommand) should map invalid data to a validation error (AAA)")
    void create_InvalidEmail_ReturnsValidationError() {
        // Act
        var result = service.handle(createCommand("not-an-email"));

        // Assert
        assertEquals("VALIDATION_ERROR", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(CreateProfileCommand) should map persistence failures to an unexpected error (AAA)")
    void create_PersistenceFailure_ReturnsUnexpectedError() {
        // Arrange
        when(repository.existsByEmailAddress(any(EmailAddress.class))).thenReturn(false);
        when(repository.save(any(Profile.class))).thenThrow(new IllegalStateException("db down"));

        // Act
        var result = service.handle(createCommand("ana@safestep.pe"));

        // Assert
        assertEquals("UNEXPECTED_ERROR", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(UpdateProfileCommand) should update the stored profile (AAA)")
    void update_ExistingProfile_IsUpdated() {
        // Arrange
        var stored = new Profile(1L, new com.safestep.platform.profiles.domain.model.valueobjects.PersonName("Old", "Name"),
                new EmailAddress("old@safestep.pe"),
                new com.safestep.platform.profiles.domain.model.valueobjects.StreetAddress("Calle", "1", "Lima", "1", "Peru"));
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        when(repository.save(stored)).thenReturn(stored);

        // Act
        var result = service.handle(new UpdateProfileCommand(1L, "Ana", "Torres", "ana@safestep.pe", "Av. Lima", "123",
                "Lima", "15001", "Peru"));

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Ana Torres", stored.getFullName());
        assertEquals("ana@safestep.pe", stored.getEmailAddress());
    }

    @Test
    @DisplayName("handle(UpdateProfileCommand) should fail for an unknown profile (AAA)")
    void update_UnknownProfile_ReturnsNotFound() {
        // Arrange
        when(repository.findById(9L)).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new UpdateProfileCommand(9L, "Ana", "Torres", "ana@safestep.pe", "Av. Lima",
                "123", "Lima", "15001", "Peru"));

        // Assert
        assertEquals("PROFILE_NOT_FOUND", errorOf(result).code());
    }

    @Test
    @DisplayName("handle(UpdateProfileCommand) should reject invalid data without saving (AAA)")
    void update_InvalidData_ReturnsValidationError() {
        // Arrange
        var stored = new Profile("Old", "Name", "old@safestep.pe", "Calle", "1", "Lima", "1", "Peru");
        when(repository.findById(1L)).thenReturn(Optional.of(stored));

        // Act
        var result = service.handle(new UpdateProfileCommand(1L, " ", "Torres", "ana@safestep.pe", "Av. Lima", "123",
                "Lima", "15001", "Peru"));

        // Assert
        assertEquals("VALIDATION_ERROR", errorOf(result).code());
        verify(repository, never()).save(any());
    }
}
