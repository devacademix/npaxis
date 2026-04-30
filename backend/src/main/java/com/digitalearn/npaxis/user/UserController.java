package com.digitalearn.npaxis.user;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.digitalearn.npaxis.utils.APIConstants.DOWNLOAD_PROFILE_PICTURE_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_ACTIVE_USER_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_ALL_ACTIVE_USERS_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_ALL_SOFT_DELETED_USERS_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_ALL_USERS_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_CURRENTLY_LOGGED_IN_USER_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_SOFT_DELETED_USER_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.HARD_DELETE_USER_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.PUT_UPDATE_USER_API;
import static com.digitalearn.npaxis.utils.APIConstants.RESTORE_USER_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.SOFT_DELETE_USER_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.UPLOAD_PROFILE_PICTURE_API;
import static com.digitalearn.npaxis.utils.APIConstants.USERS_API;


@RestController
@RequestMapping(USERS_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user management --> adding/updating new user, managing their access permissions.")
public class UserController {

    private final UserService userService;

//    @Operation(summary = "Register a new user", description = "Registers a new user with the provided details.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "User registration successful"),
//            @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors"),
//            @ApiResponse(responseCode = "500", description = "Internal Server Error - Registration failed")
//    })
//    @PostMapping(value = {USER_REGISTRATION_API, USER_REGISTRATION_API + "/"})
//    public ResponseEntity<Map<String, Object>> registerNewUser(@Valid @RequestBody UserRequestDTO userRequestDto) {
//
//        log.info("New User Registration attempt initiated for user with login: {}", userRequestDto.username());
//        UserResponseDTO userResponseDTO = this.userService.registerNewUser(userRequestDto);
//        log.info("New User Registration successful for user with login: {}", userRequestDto.username());
//
//        return ResponseHandler.generateResponse(userResponseDTO, "Registration successful", true, HttpStatus.OK);
//    }

    @Operation(summary = "Fetch currently logged in user", description = "Retrieves the details of the currently logged in user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Fetching user failed")
    })
    @GetMapping(value = {GET_CURRENTLY_LOGGED_IN_USER_API, GET_CURRENTLY_LOGGED_IN_USER_API + "/"})
    private ResponseEntity<GenericApiResponse<LoggedInUserResponseDTO>> getCurrentUser() {
        log.info("Fetching currently logged in user.");
        return ResponseHandler.generateResponse(this.userService.currentlyLoggedInUser(), "Fetching currently logged in user successful", true, HttpStatus.OK);
    }

    @Operation(summary = "Update existing user", description = "Updates the details of an existing user by user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors"),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Update failed")
    })
    @PreAuthorize("#userId == principal.userId")
    @PutMapping(value = {PUT_UPDATE_USER_API, PUT_UPDATE_USER_API + "/"})
    public ResponseEntity<GenericApiResponse<UserResponseDTO>> putUpdateExistingUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserRequestDTO userRequestDto) {
        log.info("Updating user with ID: {}", userId);
        UserResponseDTO updatedUser = this.userService.putUpdateExistingUser(userId, userRequestDto);
        log.info("User with ID: {} updated successfully", userId);
        return ResponseHandler.generateResponse(updatedUser, "User updated successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Fetch all active users", description = "Retrieves a list of all active users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Fetching users failed")
    })
    @GetMapping(value = {"", "/", GET_ALL_ACTIVE_USERS_API, GET_ALL_ACTIVE_USERS_API + "/"})
    public ResponseEntity<GenericApiResponse<List<UserResponseDTO>>> getAllActiveUsers() {
        log.info("Fetching all users");
        List<UserResponseDTO> users = this.userService.getAllActiveUsers();
        log.info("Fetched all users successfully");
        return ResponseHandler.generateResponse(users, "Users fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Fetch user by ID", description = "Retrieves a user by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Fetching user failed")
    })
    @GetMapping(value = {GET_ACTIVE_USER_BY_ID_API, GET_ACTIVE_USER_BY_ID_API + "/"})
    public ResponseEntity<GenericApiResponse<UserResponseDTO>> getActiveUserById(@PathVariable Long userId) {
        log.info("Fetching user with ID: {}", userId);
        UserResponseDTO user = this.userService.getActiveUserById(userId);
        log.info("Fetched user with ID: {} successfully", userId);
        return ResponseHandler.generateResponse(user, "User fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Delete user by ID", description = "Deletes a user by their unique ID. The user will be soft-deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Deleting user failed")
    })
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @DeleteMapping(value = {SOFT_DELETE_USER_BY_ID_API, SOFT_DELETE_USER_BY_ID_API + "/"})
    public ResponseEntity<GenericApiResponse<Object>> softRemoveUserById(@PathVariable Long userId) {
        log.info("Removing user with ID: {}", userId);
        this.userService.softRemoveUserById(userId);
        log.info("User with ID: {} removed successfully", userId);
        return ResponseHandler.generateResponse(null, "User removed successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Fetch all users including soft-deleted ones", description = "Retrieves a list of all registered users including soft-deleted ones.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Fetching users failed")
    })
    @GetMapping(value = {GET_ALL_USERS_API, GET_ALL_USERS_API + "/"})
    public ResponseEntity<GenericApiResponse<List<UserResponseDTO>>> getAllUsers() {
        log.info("Fetching all users including soft-deleted ones");
        List<UserResponseDTO> users = this.userService.getAllUsers();
        log.info("Fetched all users including soft-deleted ones successfully");
        return ResponseHandler.generateResponse(users, "Users fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Fetch all soft-deleted users", description = "Retrieves a list of all soft-deleted users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Soft-deleted users fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Fetching soft-deleted users failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = {GET_ALL_SOFT_DELETED_USERS_API, GET_ALL_SOFT_DELETED_USERS_API + "/"})
    public ResponseEntity<GenericApiResponse<List<UserResponseDTO>>> getAllSoftDeletedUsers() {
        log.info("Fetching all soft-deleted users");
        List<UserResponseDTO> users = this.userService.getAllSoftDeletedUsers();
        log.info("Fetched all soft-deleted users successfully");
        return ResponseHandler.generateResponse(users, "Soft-deleted users fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Fetch soft-deleted user by ID", description = "Retrieves a soft-deleted user by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Soft-deleted user fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Fetching soft-deleted user failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = {GET_SOFT_DELETED_USER_BY_ID_API, GET_SOFT_DELETED_USER_BY_ID_API + "/"})
    public ResponseEntity<GenericApiResponse<UserResponseDTO>> getSoftDeletedUserById(@PathVariable Long userId) {
        log.info("Fetching soft-deleted user with ID: {}", userId);
        UserResponseDTO user = this.userService.getSoftDeletedUserById(userId);
        log.info("Fetched soft-deleted user with ID: {} successfully", userId);
        return ResponseHandler.generateResponse(user, "Soft-deleted user fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Restore user by ID", description = "Restores a soft-deleted user by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User restored successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Restoring user failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = {RESTORE_USER_BY_ID_API, RESTORE_USER_BY_ID_API + "/"})
    public ResponseEntity<GenericApiResponse<Object>> restoreUserById(@PathVariable Long userId) {
        log.info("Restoring user with ID: {}", userId);
        this.userService.restoreUserById(userId);
        log.info("User with ID: {} restored successfully", userId);
        return ResponseHandler.generateResponse(null, "User restored successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Hard Delete User by ID", description = "Permanently deletes a user by their unique ID. This action is irreversible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User hard deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Hard Deleting user failed")
    })
    @DeleteMapping(value = {HARD_DELETE_USER_BY_ID_API, HARD_DELETE_USER_BY_ID_API + "/"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericApiResponse<Object>> hardDeleteUserById(@PathVariable Long userId) {
        log.info("Hard Deleting user with ID: {}", userId);
        this.userService.hardDeleteUserById(userId);
        log.info("User with ID: {} hard deleted successfully", userId);
        return ResponseHandler.generateResponse(null, "User hard deleted successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Upload profile picture", description = "Uploads a profile picture for a user by user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors"),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Upload failed")
    })
    @PreAuthorize("#userId == principal.userId")
    @PutMapping(value = {UPLOAD_PROFILE_PICTURE_API, UPLOAD_PROFILE_PICTURE_API + "/"})
    public ResponseEntity<GenericApiResponse<UserResponseDTO>> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading profile picture for user with ID: {}", userId);
        UserResponseDTO userResponseDTO = this.userService.uploadProfilePicture(userId, file);
        log.info("Profile picture uploaded successfully for user with ID: {}", userId);
        return ResponseHandler.generateResponse(userResponseDTO, "Profile picture uploaded successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Download profile picture", description = "Downloads the profile picture for a user by user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - Profile picture or user not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Download failed")
    })
    @GetMapping(value = {DOWNLOAD_PROFILE_PICTURE_API, DOWNLOAD_PROFILE_PICTURE_API + "/"})
    public ResponseEntity<org.springframework.core.io.Resource> downloadProfilePicture(@PathVariable Long userId) {
        log.info("Downloading profile picture for user with ID: {}", userId);
        org.springframework.core.io.Resource resource = userService.downloadProfilePicture(userId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Or detect based on extension if needed
                .body(resource);
    }
}
