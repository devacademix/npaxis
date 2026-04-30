package com.digitalearn.npaxis.role;

import java.util.List;

public interface RoleService {
    List<RoleResponseDTO> getAllActiveRoles();

    RoleResponseDTO getActiveRoleById(Long roleId);


}
