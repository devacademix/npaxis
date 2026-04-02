package com.digitalearn.npaxis.user;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    /**
     * Registers a new user with the provided details.
     *
     * @param userRequestDto The DTO containing user registration details.
     * @return The UserResponseDTO containing the saved user's details.
     */
    UserResponseDTO registerNewUser(@Valid UserRequestDTO userRequestDto);

    /**
     * Retrieves all users from the database.
     *
     * @return A list of UserResponseDTOs representing all users.
     */
    List<UserResponseDTO> getAllActiveUsers();

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The UserResponseDTO representing the requested user.
     */
    UserResponseDTO getActiveUserById(Long userId);

    /**
     * Updates an existing user's details.
     *
     * @param userId         The ID of the user to update.
     * @param userRequestDto The DTO containing updated user details.
     * @return The UserResponseDTO containing the updated user's details.
     */
    UserResponseDTO putUpdateExistingUser(Long userId, @Valid UserRequestDTO userRequestDto);

    /**
     * Removes a user by their ID. The user will be soft-deleted, and their details will be retained in the database.
     *
     * @param userId The ID of the user to remove.
     */
    void softRemoveUserById(Long userId);

    /**
     * Retrieves all users from the database, including soft-deleted users.
     *
     * @return A list of UserResponseDTOs representing all users.
     */
    List<UserResponseDTO> getAllUsers();

    /**
     * Retrieves a user by their ID, including if it has been soft-deleted.
     *
     * @param userId The ID of the user to retrieve.
     * @return The UserResponseDTO representing the requested user.
     */
    UserResponseDTO getUserByIdIncludingDeleted(Long userId);

    /**
     * Retrieves all soft-deleted users.
     *
     * @return A list of UserResponseDTOs representing all soft-deleted users.
     */
    List<UserResponseDTO> getAllSoftDeletedUsers();

    /**
     * Retrieves a soft-deleted user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The UserResponseDTO representing the requested soft-deleted user.
     */
    UserResponseDTO getSoftDeletedUserById(Long userId);

    /**
     * Restores a soft-deleted user by their ID.
     *
     * @param userId The ID of the user to restore.
     */
    void restoreUserById(Long userId);

    /**
     * Hard-deletes a user by their ID. The user will be permanently deleted from the database.
     *
     * @param userId The ID of the user to hard-delete.
     */
    void hardDeleteUserById(Long userId);

    UserResponseDTO uploadProfilePicture(Long userId, MultipartFile file);

    Resource downloadProfilePicture(Long userId);

    LoggedInUserResponseDTO currentlyLoggedInUser();
}
