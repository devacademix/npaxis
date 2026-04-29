package com.digitalearn.npaxis.role;

import com.digitalearn.npaxis.admin.dto.RoleCreateDTO;
import com.digitalearn.npaxis.admin.dto.RoleUpdateDTO;
import com.digitalearn.npaxis.exceptions.ResourceAlreadyExistsException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleResponseDTO> getAllActiveRoles() {
        log.info("Role Service Impl --> Retrieve All Active Roles.");
        return this.roleRepository.findAllActive().stream().map(roleMapper::roleResponseDTO).toList();
    }

    @Override
    public RoleResponseDTO getActiveRoleById(Long roleId) {
        log.info("Role Service Impl --> Retrieve Active Role by ID: {}", roleId);
        return this.roleMapper.roleResponseDTO(this.roleRepository.findActiveById(roleId).orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId)));
    }

    @Override
    @Transactional
    public RoleResponseDTO createRole(RoleCreateDTO request) {
        log.info("Creating new role: {}", request.roleName());

        // Try to parse as RoleName enum
        try {
            RoleName roleName = RoleName.valueOf(request.roleName());

            // Check if role already exists
            if (roleRepository.findByRoleName(roleName).isPresent()) {
                throw new ResourceAlreadyExistsException("Role already exists: " + request.roleName());
            }

            Role role = Role.builder()
                    .roleName(roleName)
                    .roleDescription(request.roleDescription())
                    .build();

            Role saved = roleRepository.save(role);
            log.info("Role created successfully: {}", request.roleName());
            return roleMapper.roleResponseDTO(saved);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role name: " + request.roleName() + ". Must be one of: ROLE_ADMIN, ROLE_PRECEPTOR, ROLE_STUDENT");
        }
    }

    @Override
    @Transactional
    public RoleResponseDTO updateRole(Long roleId, RoleUpdateDTO request) {
        log.info("Updating role with ID: {}", roleId);

        Role role = roleRepository.findActiveById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        if (request.roleDescription() != null) {
            role.setRoleDescription(request.roleDescription());
        }

        Role updated = roleRepository.save(role);
        log.info("Role updated successfully: {}", roleId);
        return roleMapper.roleResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        log.info("Deleting role with ID: {}", roleId);

        Role role = roleRepository.findActiveById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        // Check if role has users assigned
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role that has users assigned. Please reassign users first.");
        }

        roleRepository.softDelete(roleId);
        log.info("Role deleted successfully: {}", roleId);
    }
}
