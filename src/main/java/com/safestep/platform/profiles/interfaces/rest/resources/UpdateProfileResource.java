package com.safestep.platform.profiles.interfaces.rest.resources;

import jakarta.validation.constraints.*;

public record UpdateProfileResource(@NotBlank String firstName, @NotBlank String lastName,
        @Email @NotBlank String email, @NotBlank String street, String number, @NotBlank String city,
        @NotBlank String postalCode, @NotBlank String country) {
}
