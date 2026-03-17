package com.digitalearn.npaxis.role;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponseDTO roleResponseDTO(Role role);

    Role toRoleEntity(RoleRequestDTO roleRequestDto);
}
