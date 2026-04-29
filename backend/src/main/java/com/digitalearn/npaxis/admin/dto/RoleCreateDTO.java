package com.digitalearn.npaxis.admin.dto;

/**
 * DTO for creating a new role
 */
public record RoleCreateDTO(
        String roleName,
        String roleDescription
) {
}

