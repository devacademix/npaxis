package com.digitalearn.npaxis.user;

import java.util.Optional;

/**
 * Data Transfer Object (DTO) for User information.
 * This DTO is used to transfer user data between different layers of the application.
 *
 * @param fullName Full name of the user.
 * @param login    Login/Username of the user.
 * @param password Password for the user account (should be securely handled).
 * @param email    Email address of the user.
 */
public record UserPatchDTO(
        Optional<String> fullName,
        Optional<String> login,
        Optional<String> password,
        Optional<String> email
) {

}
