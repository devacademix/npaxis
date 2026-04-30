package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-30T19:47:51+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PreceptorMapperImpl extends PreceptorMapper {

    @Override
    public PreceptorResponseDTO toPreceptorDTO(Preceptor preceptor) {
        if ( preceptor == null ) {
            return null;
        }

        String displayName = null;
        Long userId = null;
        String credentials = null;
        String specialty = null;
        String location = null;
        String setting = null;
        List<DayOfWeekEnum> availableDays = null;
        String honorarium = null;
        String requirements = null;
        String licenseNumber = null;
        String licenseState = null;
        String licenseFileUrl = null;
        VerificationStatus verificationStatus = null;
        LocalDateTime verificationSubmittedAt = null;
        LocalDateTime verificationReviewedAt = null;

        displayName = preceptorUserDisplayName( preceptor );
        userId = preceptor.getUserId();
        credentials = preceptor.getCredentials();
        specialty = preceptor.getSpecialty();
        location = preceptor.getLocation();
        setting = preceptor.getSetting();
        Set<DayOfWeekEnum> set = preceptor.getAvailableDays();
        if ( set != null ) {
            availableDays = new ArrayList<DayOfWeekEnum>( set );
        }
        honorarium = preceptor.getHonorarium();
        requirements = preceptor.getRequirements();
        licenseNumber = preceptor.getLicenseNumber();
        licenseState = preceptor.getLicenseState();
        licenseFileUrl = preceptor.getLicenseFileUrl();
        verificationStatus = preceptor.getVerificationStatus();
        verificationSubmittedAt = preceptor.getVerificationSubmittedAt();
        verificationReviewedAt = preceptor.getVerificationReviewedAt();

        boolean isVerified = false;
        boolean isPremium = false;

        PreceptorResponseDTO preceptorResponseDTO = new PreceptorResponseDTO( userId, displayName, credentials, specialty, location, setting, availableDays, honorarium, requirements, isVerified, isPremium, licenseNumber, licenseState, licenseFileUrl, verificationStatus, verificationSubmittedAt, verificationReviewedAt );

        return preceptorResponseDTO;
    }

    @Override
    public Preceptor toPreceptorEntity(PreceptorRequestDTO preceptorRequestDto) {
        if ( preceptorRequestDto == null ) {
            return null;
        }

        Preceptor.PreceptorBuilder<?, ?> preceptor = Preceptor.builder();

        List<DayOfWeekEnum> list = preceptorRequestDto.availableDays();
        if ( list != null ) {
            preceptor.availableDays( new LinkedHashSet<DayOfWeekEnum>( list ) );
        }
        preceptor.credentials( preceptorRequestDto.credentials() );
        preceptor.email( preceptorRequestDto.email() );
        preceptor.honorarium( preceptorRequestDto.honorarium() );
        preceptor.licenseFileUrl( preceptorRequestDto.licenseFileUrl() );
        preceptor.licenseNumber( preceptorRequestDto.licenseNumber() );
        preceptor.licenseState( preceptorRequestDto.licenseState() );
        preceptor.location( preceptorRequestDto.location() );
        preceptor.name( preceptorRequestDto.name() );
        preceptor.phone( preceptorRequestDto.phone() );
        preceptor.requirements( preceptorRequestDto.requirements() );
        preceptor.setting( preceptorRequestDto.setting() );
        preceptor.specialty( preceptorRequestDto.specialty() );

        return preceptor.build();
    }

    private String preceptorUserDisplayName(Preceptor preceptor) {
        User user = preceptor.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getDisplayName();
    }
}
