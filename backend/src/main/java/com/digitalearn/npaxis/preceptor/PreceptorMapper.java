package com.digitalearn.npaxis.preceptor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper class for converting Preceptor entities to Preceptor DTOs and vice-versa.
 */
@Mapper(componentModel = "spring")
public abstract class PreceptorMapper {

    // Entity to DTO mapping
    @Mapping(target = "displayName", source = "user.displayName")
    @Mapping(target = "isPremium", source = "preceptor.premium")
    @Mapping(target = "isVerified", source = "preceptor.verified")
    public abstract PreceptorResponseDTO toPreceptorDTO(Preceptor preceptor);

    // DTO to Entity mapping
    public abstract Preceptor toPreceptorEntity(PreceptorRequestDTO preceptorRequestDto);
}
