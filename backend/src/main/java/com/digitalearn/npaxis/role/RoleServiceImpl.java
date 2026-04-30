package com.digitalearn.npaxis.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
