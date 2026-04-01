package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import com.digitalearn.npaxis.exceptions.BusinessException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Implementation of PreceptorService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PreceptorServiceImpl implements PreceptorService {

    private final PreceptorRepository preceptorRepository;
    private final PreceptorMapper preceptorMapper;

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

        String storedPath = storeLicenseFile(file, userId);

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
                .email(preceptor.getEmail())
                .build();
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

    private String storeLicenseFile(MultipartFile file, Long userId) {
        String originalName = file.getOriginalFilename() == null ? "license" : file.getOriginalFilename();
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0 && dot < originalName.length() - 1) {
            extension = originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
        }

        if (!extension.isEmpty() && !extension.equals("pdf") && !extension.equals("png") && !extension.equals("jpg") && !extension.equals("jpeg")) {
            throw new IllegalArgumentException("Only PDF, PNG, JPG, and JPEG files are allowed.");
        }

        try {
            Path uploadDir = Path.of("uploads", "licenses");
            Files.createDirectories(uploadDir);

            String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String filename = "preceptor-" + userId + "-" + UUID.randomUUID() + "-" + safeName;
            Path target = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded license file.", ex);
        }
    }
}
