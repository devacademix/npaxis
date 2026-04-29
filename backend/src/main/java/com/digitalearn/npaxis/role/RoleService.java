package com.digitalearn.npaxis.role;

import com.digitalearn.npaxis.admin.dto.RoleCreateDTO;
import com.digitalearn.npaxis.admin.dto.RoleUpdateDTO;

import java.util.List;

public interface RoleService {
    List<RoleResponseDTO> getAllActiveRoles();

    RoleResponseDTO getActiveRoleById(Long roleId);

    /**
     * Create a new role
     */
    RoleResponseDTO createRole(RoleCreateDTO request);

    /**
     * Update a role
     */
    RoleResponseDTO updateRole(Long roleId, RoleUpdateDTO request);

    /**
     * Delete a role
     */
    void deleteRole(Long roleId);
}
