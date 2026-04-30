package com.digitalearn.npaxis.admin.dto;

/**
 * DTO for assigning a role to a user
 */
public record UserRoleAssignmentDTO(
        Long userId,
        Long roleId
) {
}

