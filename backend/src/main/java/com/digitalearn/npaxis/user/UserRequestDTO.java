package com.digitalearn.npaxis.user;

import com.digitalearn.npaxis.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;

import java.util.Set;

/**
 * Data Transfer Object (DTO) for User information.
 * This DTO is used to transfer user data between different layers of the application.
 *
 * @param fullName Full name of the user.
 * @param username Login/Username of the user.
 * @param password Password for the user account (should be securely handled).
 * @param email    Email address of the user.
 * @param roles    Set of roles associated with the user.
 */
public record UserRequestDTO(
        @NonNull @NotBlank @NotEmpty
        String fullName,
        @NonNull @NotBlank @NotEmpty
        String username,
        @NonNull @NotBlank @NotEmpty
        @ValidPassword
        String password,
        @Email(message = "Email should be a valid email format")
        String email,
        Set<Long> roles
) {

}
