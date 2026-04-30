package com.digitalearn.npaxis.role;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-30T19:47:50+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleResponseDTO roleResponseDTO(Role role) {
        if ( role == null ) {
            return null;
        }

        Long roleId = null;
        RoleName roleName = null;

        roleId = role.getRoleId();
        roleName = role.getRoleName();

        String description = null;

        RoleResponseDTO roleResponseDTO = new RoleResponseDTO( roleId, roleName, description );

        return roleResponseDTO;
    }

    @Override
    public Role toRoleEntity(RoleRequestDTO roleRequestDto) {
        if ( roleRequestDto == null ) {
            return null;
        }

        Role.RoleBuilder<?, ?> role = Role.builder();

        if ( roleRequestDto.roleName() != null ) {
            role.roleName( Enum.valueOf( RoleName.class, roleRequestDto.roleName() ) );
        }

        return role.build();
    }
}
