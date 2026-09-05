package com.safestep.platform.profiles.interfaces.rest;

import com.safestep.platform.profiles.application.commandservices.ProfileCommandService;
import com.safestep.platform.profiles.application.queryservices.ProfileQueryService;
import com.safestep.platform.profiles.domain.model.queries.GetAllProfilesQuery;
import com.safestep.platform.profiles.domain.model.queries.GetProfileByEmailQuery;
import com.safestep.platform.profiles.domain.model.queries.GetProfileByIdQuery;
import com.safestep.platform.profiles.domain.model.commands.CreateProfileCommand;
import com.safestep.platform.profiles.domain.model.commands.UpdateProfileCommand;
import com.safestep.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.safestep.platform.profiles.interfaces.rest.resources.CreateProfileResource;
import com.safestep.platform.profiles.interfaces.rest.resources.ProfileResource;
import com.safestep.platform.profiles.interfaces.rest.resources.UpdateProfileResource;
import com.safestep.platform.profiles.interfaces.rest.transform.CreateProfileCommandFromResourceAssembler;
import com.safestep.platform.profiles.interfaces.rest.transform.ProfileResourceFromEntityAssembler;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.services.CurrentUserService;
import com.safestep.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.safestep.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * REST controller that exposes profile resources and profile retrieval endpoints.
 */
@RestController
@RequestMapping(value = "/api/v1/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile management endpoints")
public class ProfilesController {
    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;
    private final CurrentUserService currentUserService;

    /**
     * Constructor
     *
     * @param profileCommandService
     *            The {@link ProfileCommandService} instance
     * @param profileQueryService
     *            The {@link ProfileQueryService} instance
     */
    public ProfilesController(ProfileCommandService profileCommandService, ProfileQueryService profileQueryService,
            CurrentUserService currentUserService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current SafeStep profile", description = "Retrieves the SafeStep profile associated with the authenticated user.")
    public ResponseEntity<?> getMyProfile() {
        var profile = profileQueryService.handle(new GetProfileByEmailQuery(new EmailAddress(currentUserService.username())));
        if (profile.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current SafeStep profile", description = "Updates the SafeStep profile associated with the authenticated user.")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UpdateProfileResource payload) {
        var username = currentUserService.username();
        var email = payload.email() == null || payload.email().isBlank() ? username : payload.email();
        var profile = profileQueryService.handle(new GetProfileByEmailQuery(new EmailAddress(username)));
        var result = profile
                .map(value -> profileCommandService.handle(new UpdateProfileCommand(value.getId(), payload.firstName(),
                        payload.lastName(), email, payload.street(), payload.number(), payload.city(),
                        payload.postalCode(), payload.country())))
                .orElseGet(() -> profileCommandService.handle(new CreateProfileCommand(payload.firstName(),
                        payload.lastName(), email, payload.street(), payload.number(), payload.city(),
                        payload.postalCode(), payload.country())));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                ProfileResourceFromEntityAssembler::toResourceFromEntity, profile.isPresent() ? HttpStatus.OK : HttpStatus.CREATED);
    }

    /**
     * Create a new profile
     *
     * @param resource
     *            The {@link CreateProfileResource} instance
     *
     * @return A {@link ProfileResource} resource for the created profile
     */
    @PostMapping
    @Operation(summary = "Create a new profile", description = "Creates a new user profile with contact and address information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profile created successfully", content = @Content(schema = @Schema(implementation = ProfileResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Conflict - profile already exists") })
    public ResponseEntity<?> createProfile(@Valid @RequestBody CreateProfileResource resource) {
        var createProfileCommand = CreateProfileCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = profileCommandService.handle(createProfileCommand);

        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                ProfileResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    /**
     * Get a profile by ID
     *
     * @param profileId
     *            The profile ID
     *
     * @return A {@link ProfileResource} resource for the profile
     */
    @GetMapping("/{profileId}")
    @Operation(summary = "Get profile by ID", description = "Retrieves a specific user profile's information by unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found", content = @Content(schema = @Schema(implementation = ProfileResource.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found") })
    public ResponseEntity<?> getProfileById(
            @PathVariable @Parameter(description = "Profile unique identifier", example = "1", required = true) Long profileId) {
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if (profile.isEmpty()) {
            var error = ApplicationError.notFound("Profile", profileId.toString());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        }
        var profileEntity = profile.get();
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profileEntity);
        return ResponseEntity.ok(profileResource);
    }

    /**
     * Get all profiles
     *
     * @return A list of {@link ProfileResource} resources for all profiles
     */
    @GetMapping
    @Operation(summary = "Get all profiles", description = "Retrieves a list of all user profiles in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profiles found", content = @Content(schema = @Schema(implementation = ProfileResource.class))) })
    public ResponseEntity<List<ProfileResource>> getAllProfiles() {
        var profiles = profileQueryService.handle(new GetAllProfilesQuery());
        if (profiles.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        var profileResources = profiles.stream().map(ProfileResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(profileResources);
    }

}
