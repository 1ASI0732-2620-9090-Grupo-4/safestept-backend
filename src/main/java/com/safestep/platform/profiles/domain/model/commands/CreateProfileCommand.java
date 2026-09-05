package com.safestep.platform.profiles.domain.model.commands;

/**
 * Create Profile Command
 */
public record CreateProfileCommand(String firstName, String lastName, String email, String street, String number,
        String city, String postalCode, String country) {
}
