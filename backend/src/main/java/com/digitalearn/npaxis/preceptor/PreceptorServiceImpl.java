package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.analytics.TrackEvent;
import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import com.digitalearn.npaxis.exceptions.BusinessException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Implementation of PreceptorService.
 * <p>
 * ============================================
 * ANALYTICS TRACKING
 * ============================================
 * <p>
 * This service is instrumented with @TrackEvent annotations to automatically
 * capture analytics for key business operations:
 * <p>
 * - PROFILE_VIEWED: when profile details are accessed
 * - PROFILE_LIST_VIEWED: when search results are retrieved
 * - CONTACT_REVEALED: when contact info is accessed
 * - RESOURCE_DOWNLOADED: when license files are downloaded
 * <p>
 * Events are tracked asynchronously without blocking business logic.
 * See ANALYTICS_INTEGRATION_GUIDE for more details.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PreceptorServiceImpl implements PreceptorService {

    private final PreceptorRepository preceptorRepository;
    private final PreceptorMapper preceptorMapper;
    private final StorageService storageService;
    private final CredentialService credentialService;
    private final SpecialtyService specialtyService;

    /**
     * Searches and filters preceptors.
     * <p>
     * ANALYTICS:
     * - Tracks PROFILE_LIST_VIEWED event
     * - Captures search query and filter criteria in metadata
     * - Useful for understanding user discovery patterns
     */
    @TrackEvent(
            eventType = EventType.PROFILE_LIST_VIEWED,
            metadataExpression = "{'specialty': #filter.specialty, " +
                    "'location': #filter.location, " +
                    "'minHonorarium': #filter.minHonorarium, " +
                    "'maxHonorarium': #filter.maxHonorarium, " +
                    "'resultCount': #result.getNumberOfElements()}"
    )
    @Transactional(readOnly = true)
    @Override
    public Page<PreceptorResponseDTO> searchPreceptors(
            PreceptorFilter filter,
            Pageable pageable
    ) {

        log.debug("Searching preceptors with filters + pagination");

        Specification<Preceptor> spec = this.buildPreceptorSpec(filter);

        Page<Preceptor> page = preceptorRepository.findAll(spec, pageable);

        return page.map(preceptorMapper::toPreceptorDTO);
    }

//    @Transactional(readOnly = true)
//    @Override
//    public List<PreceptorResponseDTO> getAllActivePreceptors(PreceptorFilter filter,
//                                                             Pageable pageable) {
//        log.debug("Preceptor Service Impl --> Get all active preceptors");
//        return preceptorRepository.findAllActive().stream()
//                .map(preceptorMapper::toPreceptorDTO)
//                .toList();
//    }

    @Override
    @Transactional(readOnly = true)
    @TrackEvent(
            eventType = EventType.PROFILE_VIEWED,
            targetIdExpression = "#userId.toString()"
    )
    public PreceptorResponseDTO getActivePreceptorById(Long userId) {
        log.debug("Preceptor Service Impl --> Get active preceptor by ID: {}", userId);


        Preceptor preceptor = preceptorRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        log.info("Preceptor: isPremium{}, isVerified: {}", preceptor.isPremium(), preceptor.isVerified());

        return preceptorMapper.toPreceptorDTO(preceptor);
    }

    @Override
    @Transactional
    public PreceptorResponseDTO updatePreceptor(Long userId, PreceptorRequestDTO preceptorRequestDto) {
        log.debug("Preceptor Service Impl --> Update preceptor by ID: {}", userId);
        Preceptor existingPreceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        Preceptor updatedPreceptor = preceptorMapper.toPreceptorEntity(preceptorRequestDto);
        updatedPreceptor.setUserId(userId);
        updatedPreceptor.setUser(existingPreceptor.getUser());

        // Handle credentials - get or create based on the provided names
        if (preceptorRequestDto.credentials() != null && !preceptorRequestDto.credentials().isEmpty()) {
            updatedPreceptor.setCredentials(credentialService.getOrCreateCredentials(
                    new java.util.HashSet<>(preceptorRequestDto.credentials())));
        }

        // Handle specialties - get or create based on the provided names
        if (preceptorRequestDto.specialties() != null && !preceptorRequestDto.specialties().isEmpty()) {
            updatedPreceptor.setSpecialties(specialtyService.getOrCreateSpecialties(
                    new java.util.HashSet<>(preceptorRequestDto.specialties())));
        }

        // Preserve statuses and flags
        updatedPreceptor.setVerified(existingPreceptor.isVerified());
        updatedPreceptor.setPremium(existingPreceptor.isPremium());
        updatedPreceptor.setVerificationStatus(existingPreceptor.getVerificationStatus());
        updatedPreceptor.setVerificationSubmittedAt(existingPreceptor.getVerificationSubmittedAt());
        updatedPreceptor.setVerificationReviewedAt(existingPreceptor.getVerificationReviewedAt());

        Preceptor savedPreceptor = preceptorRepository.save(updatedPreceptor);
        return preceptorMapper.toPreceptorDTO(savedPreceptor);
    }

    @Override
    @Transactional
    public void softDeletePreceptor(Long userId) {
        log.debug("Preceptor Service Impl --> Soft delete preceptor by ID: {}", userId);
        preceptorRepository.softDelete(userId);
    }

    @Override
    @Transactional
    public void hardDeletePreceptor(Long userId) {
        log.debug("Preceptor Service Impl --> Hard delete preceptor by ID: {}", userId);
        preceptorRepository.hardDelete(userId);
    }

    @Override
    @Transactional
    public void restorePreceptor(Long userId) {
        log.debug("Preceptor Service Impl --> Restore preceptor by ID: {}", userId);
        preceptorRepository.restore(userId);
    }

    @Override
    @Transactional
    public PreceptorResponseDTO verifyPreceptor(Long userId) {
        log.debug("Preceptor Service Impl --> Verify preceptor by ID: {}", userId);
        Preceptor preceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        preceptor.setVerified(true);
        preceptor.setVerificationStatus(VerificationStatus.APPROVED);
        preceptor.setVerificationReviewedAt(LocalDateTime.now());

        Preceptor savedPreceptor = preceptorRepository.save(preceptor);
        return preceptorMapper.toPreceptorDTO(savedPreceptor);
    }

    @Override
    @Transactional
    public PreceptorResponseDTO submitLicense(Long userId, PreceptorRequestDTO preceptorRequestDto) {
        log.debug("Preceptor Service Impl --> Submit license for preceptor ID: {}", userId);
        Preceptor preceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        preceptor.setLicenseNumber(preceptorRequestDto.licenseNumber());
        preceptor.setLicenseState(preceptorRequestDto.licenseState());
        preceptor.setLicenseFileUrl(preceptorRequestDto.licenseFileUrl());
        preceptor.setVerificationStatus(VerificationStatus.PENDING);
        preceptor.setVerificationSubmittedAt(LocalDateTime.now());

        Preceptor savedPreceptor = preceptorRepository.save(preceptor);
        return preceptorMapper.toPreceptorDTO(savedPreceptor);
    }

    @Override
    @Transactional
    public PreceptorResponseDTO submitLicense(Long userId, String licenseNumber, String licenseState, MultipartFile file) {
        log.debug("Preceptor Service Impl --> Submit multipart license for preceptor ID: {}", userId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A license file is required.");
        }

        Preceptor preceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        // If replacing an existing license, delete the old file
        if (preceptor.getLicenseFileUrl() != null && !preceptor.getLicenseFileUrl().isEmpty()) {
            storageService.deleteFile(preceptor.getLicenseFileUrl());
        }

        String storedPath = storageService.storeFile(file, "licenses", userId.toString());

        preceptor.setLicenseNumber(licenseNumber);
        preceptor.setLicenseState(licenseState);
        preceptor.setLicenseFileUrl(storedPath);
        preceptor.setVerificationStatus(VerificationStatus.PENDING);
        preceptor.setVerificationSubmittedAt(LocalDateTime.now());

        Preceptor savedPreceptor = preceptorRepository.save(preceptor);
        return preceptorMapper.toPreceptorDTO(savedPreceptor);
    }

    @Override
    @Transactional(readOnly = true)
    @TrackEvent(
            eventType = EventType.CONTACT_REVEALED,
            targetIdExpression = "#userId.toString()"
    )
    public PreceptorContactResponseDTO revealContact(Long userId) {
        log.debug("Preceptor Service Impl --> Reveal contact for preceptor ID: {}", userId);
        Preceptor preceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        // check if the preceptor has premium or not
        if (!preceptor.isPremium()) {
            throw new BusinessException(BusinessErrorCodes.PRECEPTOR_NOT_PREMIUM);
        }
        return PreceptorContactResponseDTO.builder()
                .phone(preceptor.getPhone())
                .email(preceptor.getUser().getEmail())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @TrackEvent(
            eventType = EventType.RESOURCE_DOWNLOADED,
            targetIdExpression = "#userId.toString()",
            metadataExpression = "{'resourceType': 'license_file'}"
    )
    public Resource downloadLicense(Long userId) {
        log.debug("Preceptor Service Impl --> Download license for preceptor ID: {}", userId);
        Preceptor preceptor = preceptorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + userId));

        if (preceptor.getLicenseFileUrl() == null || preceptor.getLicenseFileUrl().isEmpty()) {
            throw new ResourceNotFoundException("License file not found for preceptor ID: " + userId);
        }

        return storageService.loadFileAsResource(preceptor.getLicenseFileUrl());
    }

    private Specification<Preceptor> buildPreceptorSpec(PreceptorFilter filter) {
        return Specification.where(PreceptorSpecification.isActive())
                .and(PreceptorSpecification.isNotDeleted())
                .and(PreceptorSpecification.hasSpecialty(filter.getSpecialty()))
                .and(PreceptorSpecification.hasLocation(filter.getLocation()))
                .and(PreceptorSpecification.hasAvailableDays(filter.getAvailableDays()))
                .and(PreceptorSpecification.honorariumBetween(
                        filter.getMinHonorarium(),
                        filter.getMaxHonorarium()
                ));
    }
}
