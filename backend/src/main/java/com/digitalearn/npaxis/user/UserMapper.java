package com.digitalearn.npaxis.user;


import com.digitalearn.npaxis.role.Role;
import com.digitalearn.npaxis.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper class for converting User entities to User DTOs.
 * This class provides a static method to convert a User entity to a UserDto.
 */

@Mapper(componentModel = "spring")
@RequiredArgsConstructor
public abstract class UserMapper {
    @Autowired
    private RoleRepository roleRepository;

    // DTO to Entity mapping
    public abstract User toUserEntity(UserRequestDTO userRequestDto);

    //    Entity to DTO mapping
    public abstract UserResponseDTO toUserDTO(User user);

    //    Entity to Logged-In User DTO mapping
    public abstract LoggedInUserResponseDTO toLoggedInUserDTO(User user);

    /**
     * Custom mapping from a role name (String) to a Role entity.
     * Used when converting a UserRequestDto to a User entity.
     */
    protected Role roleIdToRole(Long roleId) {
        if (roleId == null) {
            return null;
        }
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Critical Error: Role with ID:: '" + roleId + "' not found in the database."));
    }

    /**
     * Custom mapping from a Role entity to its name (String).
     * Used when converting a User entity to a UserResponseDTO.
     */
    protected String roleToRoleString(Role role) {
        if (role == null) {
            return null;
        }
        // Assuming your Role entity has a 'getName()' method
        return role.getRoleName().name();
    }
}
