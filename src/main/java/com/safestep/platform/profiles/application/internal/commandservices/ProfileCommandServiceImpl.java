package com.safestep.platform.profiles.application.internal.commandservices;

import com.safestep.platform.profiles.application.commandservices.ProfileCommandService;
import com.safestep.platform.profiles.domain.model.aggregates.Profile;
import com.safestep.platform.profiles.domain.model.commands.CreateProfileCommand;
import com.safestep.platform.profiles.domain.model.commands.UpdateProfileCommand;
import com.safestep.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.safestep.platform.profiles.domain.repositories.ProfileRepository;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

/**
 * Profile Command Service Implementation
 */
@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {
    private final ProfileRepository profileRepository;

    /**
     * Constructor
     *
     * @param profileRepository
     *            The {@link ProfileRepository} instance
     */
    public ProfileCommandServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // inherited javadoc
    @Override
    public Result<Profile, ApplicationError> handle(CreateProfileCommand command) {
        try {
            var emailAddress = new EmailAddress(command.email());
            if (profileRepository.existsByEmailAddress(emailAddress)) {
                return Result.failure(ApplicationError.conflict("Profile",
                        "A profile with email address '%s' already exists".formatted(command.email())));
            }

            var profile = new Profile(command);
            var savedProfile = profileRepository.save(profile);
            return Result.success(savedProfile);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Profile", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Profile creation", e.getMessage()));
        }
    }

    @Override
    public Result<Profile, ApplicationError> handle(UpdateProfileCommand command) {
        try {
            var profile = profileRepository.findById(command.profileId());
            if (profile.isEmpty())
                return Result.failure(ApplicationError.notFound("Profile", command.profileId().toString()));
            profile.get().update(command.firstName(), command.lastName(), command.email(), command.street(),
                    command.number(), command.city(), command.postalCode(), command.country());
            return Result.success(profileRepository.save(profile.get()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Profile", e.getMessage()));
        }
    }
}
