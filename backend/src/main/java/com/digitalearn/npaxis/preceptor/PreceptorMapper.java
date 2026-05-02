package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.utils.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper class for converting Preceptor entities to Preceptor DTOs and vice-versa.
 */
@Mapper(componentModel = "spring")
public abstract class PreceptorMapper {

    // Entity to DTO mapping
    @Mapping(target = "displayName", source = "preceptor", qualifiedByName = "mapDisplayName")
    @Mapping(target = "credentials", source = "preceptor.credentials", qualifiedByName = "mapCredentials")
    @Mapping(target = "specialties", source = "preceptor.specialties", qualifiedByName = "mapSpecialties")
    @Mapping(target = "isPremium", source = "preceptor.premium")
    @Mapping(target = "isVerified", source = "preceptor.verified")
    public abstract PreceptorResponseDTO toPreceptorDTO(Preceptor preceptor);

    // DTO to Entity mapping
    // Note: credentials and specialties are handled by the service layer via
    // CredentialService.getOrCreateCredentials() and SpecialtyService.getOrCreateSpecialties()
    @Mapping(target = "credentials", ignore = true)
    @Mapping(target = "specialties", ignore = true)
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

    /**
     * Maps Credential entities to their string names
     */
    @Named("mapCredentials")
    protected List<String> mapCredentials(Set<Credential> credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return List.of();
        }

        return credentials.stream()
                .map(Credential::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Maps Specialty entities to their string names
     */
    @Named("mapSpecialties")
    protected List<String> mapSpecialties(Set<Specialty> specialties) {
        if (specialties == null || specialties.isEmpty()) {
            return List.of();
        }

        return specialties.stream()
                .map(Specialty::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
