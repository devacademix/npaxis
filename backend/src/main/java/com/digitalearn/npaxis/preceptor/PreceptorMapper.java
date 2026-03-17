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
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "preceptorEmail", source = "email") // Mapping preceptor's specific email
    public abstract PreceptorResponseDTO toPreceptorDTO(Preceptor preceptor);

    // DTO to Entity mapping
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "isPremium", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "verificationSubmittedAt", ignore = true)
    @Mapping(target = "verificationReviewedAt", ignore = true)
    public abstract Preceptor toPreceptorEntity(PreceptorRequestDTO preceptorRequestDto);
}
