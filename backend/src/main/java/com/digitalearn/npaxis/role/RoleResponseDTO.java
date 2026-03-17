package com.digitalearn.npaxis.role;

public record RoleResponseDTO(
        Long roleId,
        RoleName roleName,
        String description
) {
}
