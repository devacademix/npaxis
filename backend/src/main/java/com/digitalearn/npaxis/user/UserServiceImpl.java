package com.digitalearn.npaxis.user;

import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.storage.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    /**
     * Registers a new user by converting the UserRequestDto to a User entity,
     * encoding the password, and saving it to the database.
     *
     * @param userRequestDto The DTO containing user registration details.
     * @return The UserResponseDTO containing the saved user's details.
     */
    @Override
    @Transactional
    public UserResponseDTO registerNewUser(@Valid UserRequestDTO userRequestDto) {
        log.debug("User Service Impl --> Register a new User");
        User userEntity = this.userMapper.toUserEntity(userRequestDto);
        userEntity.setPassword(this.passwordEncoder.encode(userRequestDto.password()));
        log.debug("User Request DTO to User Entity");
        User savedUser = this.userRepository.save(userEntity);
        log.debug("User Entity saved to DB");
        return this.userMapper.toUserDTO(savedUser);
    }


    @Override
    public LoggedInUserResponseDTO currentlyLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return this.userMapper.toLoggedInUserDTO(user);
    }

    /**
     * Retrieves all users from the database and maps them to UserResponseDTOs.
     *
     * @return A list of UserResponseDTOs representing all users.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllActiveUsers() {
        return this.userRepository.findAllActive().stream()
                .map(this.userMapper::toUserDTO).toList();
    }

    /**
     * Retrieves a user by their ID and maps it to a UserResponseDTO.
     *
     * @param userId The ID of the user to retrieve.
     * @return The UserResponseDTO containing the user's details.
     * @throws ResourceNotFoundException If the user with the given ID does not exist.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getActiveUserById(Long userId) {
        User user = this.userRepository.findActiveById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return this.userMapper.toUserDTO(user);
    }

    /**
     * Updates an existing user by converting the UserRequestDto to a User entity,
     * encoding the password, and saving it to the database.
     *
     * @param userId         The ID of the user to update.
     * @param userRequestDto The DTO containing updated user details.
     * @return The UserResponseDTO containing the updated user's details.
     */
    @Override
    @Transactional
    public UserResponseDTO putUpdateExistingUser(Long userId, UserRequestDTO userRequestDto) {
        log.debug("User Service Impl --> Update Existing User");
        this.userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        User user = this.userMapper.toUserEntity(userRequestDto);
        user.setUserId(userId);
        log.debug("User Request DTO to User Entity for update");
        user.setPassword(this.passwordEncoder.encode(userRequestDto.password()));
        log.debug("Password encoded for update");
        User updatedUser = this.userRepository.save(user);
        return this.userMapper.toUserDTO(updatedUser);
    }

    /**
     * Removes a user by their ID. The user will be soft-deleted, and their details will be retained in the database.
     *
     * @param userId The ID of the user to remove.
     */
    @Override
    public void softRemoveUserById(Long userId) {
        log.debug("User Service Impl --> Remove User by ID: {}", userId);
        userRepository.softDelete(userId);
        log.info("User with ID: {} has been deactivated", userId);
    }

    /**
     * Retrieves all users from the database, including soft-deleted users.
     *
     * @return A list of UserResponseDTOs representing all users.
     */
    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.debug("User Service Impl --> Retrieve All Users including soft-deleted.");
        return this.userRepository.findAllIncludingDeleted().stream()
                .map(this.userMapper::toUserDTO).toList();
    }

    /**
     * Retrieves a user by their ID, including if it has been soft-deleted.
     *
     * @param userId The ID of the user to retrieve.
     * @return The UserResponseDTO representing the requested user.
     */
    @Override
    public UserResponseDTO getUserByIdIncludingDeleted(Long userId) {
        log.debug("User Service Impl --> Retrieve User by ID: {} including soft-deleted.", userId);
        return this.userMapper.toUserDTO(
                this.userRepository.findByIdIncludingDeleted(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId)));
    }

    /**
     * Retrieves all soft-deleted users from the database.
     *
     * @return A list of UserResponseDTOs representing all soft-deleted users.
     */
    @Override
    public List<UserResponseDTO> getAllSoftDeletedUsers() {
        log.debug("User Service Impl --> Retrieve All Soft-Deleted Users.");
        return this.userRepository.findAllDeleted().stream()
                .map(this.userMapper::toUserDTO).toList();
    }

    @Override
    public UserResponseDTO getSoftDeletedUserById(Long userId) {
        log.debug("User Service Impl --> Retrieve Soft-Deleted User by ID: {}", userId);
        return this.userMapper.toUserDTO(this.userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId)));

    }

    @Override
    public void restoreUserById(Long userId) {
        log.debug("User Service Impl --> Restore User by ID: {}", userId);
        this.userRepository.restore(userId);
    }

    @Override
    public void hardDeleteUserById(Long userId) {
        log.debug("User Service Impl --> Hard Delete User by ID: {}", userId);
        this.userRepository.hardDeleteById(userId);
    }

    @Override
    @Transactional
    public UserResponseDTO uploadProfilePicture(Long userId, MultipartFile file) {
        log.debug("User Service Impl --> Upload profile picture for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // If replacing an existing profile picture, delete the old file
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            storageService.deleteFile(user.getPhotoUrl());
        }

        String photoUrl = storageService.storeFile(file, "profile-pictures", userId.toString());
        user.setPhotoUrl(photoUrl);

        User savedUser = userRepository.save(user);
        return userMapper.toUserDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadProfilePicture(Long userId) {
        log.debug("User Service Impl --> Download profile picture for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getPhotoUrl() == null || user.getPhotoUrl().isEmpty()) {
            throw new ResourceNotFoundException("Profile picture not found for user ID: " + userId);
        }

        return storageService.loadFileAsResource(user.getPhotoUrl());
    }
}
