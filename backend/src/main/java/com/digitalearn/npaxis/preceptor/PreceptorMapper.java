package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.utils.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper class for converting Preceptor entities to Preceptor DTOs and vice-versa.
 */
@Mapper(componentModel = "spring")
public abstract class PreceptorMapper {

    // Entity to DTO mapping
    @Mapping(target = "displayName", source = "preceptor", qualifiedByName = "mapDisplayName")
    @Mapping(target = "isPremium", source = "preceptor.premium")
    @Mapping(target = "isVerified", source = "preceptor.verified")
    public abstract PreceptorResponseDTO toPreceptorDTO(Preceptor preceptor);

    // DTO to Entity mapping
    public abstract Preceptor toPreceptorEntity(PreceptorRequestDTO preceptorRequestDto);

    /**
     * Maps the displayName based on premium status.
     * If preceptor is premium, shows the full displayName.
     * If not premium, shows only initials (first letter of first and last name, or just first letter if only one name).
     */
    @Named("mapDisplayName")
    protected String mapDisplayName(Preceptor preceptor) {
        if (preceptor == null || preceptor.getUser() == null) {
            return null;
        }

        String fullName = preceptor.getUser().getDisplayName();
        if (StringUtils.isEmpty(fullName)) {
            return fullName;
        }

        // If premium, show full name
        if (preceptor.isPremium()) {
            return fullName;
        }

        // If not premium, show only initials
        return StringUtils.getInitials(fullName);
    }
}
