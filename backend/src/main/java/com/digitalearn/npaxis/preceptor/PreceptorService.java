package com.digitalearn.npaxis.preceptor;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for managing Preceptor details.
 */
public interface PreceptorService {

    @Transactional(readOnly = true)
    Page<PreceptorResponseDTO> searchPreceptors(
            PreceptorFilter filter,
            Pageable pageable
    );

//    /**
//     * Retrieves all active preceptors.
//     *
//     * @return List of PreceptorResponseDTOs.
//     */
//    @Transactional(readOnly = true)
//    List<PreceptorResponseDTO> getAllActivePreceptors(PreceptorFilter filter,
//                                                      Pageable pageable);

    /**
     * Retrieves an active preceptor by their ID.
     *
     * @param userId The ID of the preceptor.
     * @return PreceptorResponseDTO.
     */
    PreceptorResponseDTO getActivePreceptorById(Long userId);

    /**
     * Updates an existing preceptor's details.
     *
     * @param userId              The ID of the preceptor.
     * @param preceptorRequestDto Updated details.
     * @return Updated PreceptorResponseDTO.
     */
    PreceptorResponseDTO updatePreceptor(Long userId, @Valid PreceptorRequestDTO preceptorRequestDto);

    /**
     * Soft deletes a preceptor.
     *
     * @param userId The ID of the preceptor.
     */
    void softDeletePreceptor(Long userId);

    /**
     * Hard deletes a preceptor.
     *
     * @param userId The ID of the preceptor.
     */
    void hardDeletePreceptor(Long userId);

    /**
     * Restores a soft-deleted preceptor.
     *
     * @param userId The ID of the preceptor.
     */
    void restorePreceptor(Long userId);

    /**
     * Verifies a preceptor (admin action).
     *
     * @param userId The ID of the preceptor.
     * @return Updated PreceptorResponseDTO.
     */
    PreceptorResponseDTO verifyPreceptor(Long userId);

    /**
     * Submits a license for verification.
     *
     * @param userId              The ID of the preceptor.
     * @param preceptorRequestDto DTO containing license information.
     * @return Updated PreceptorResponseDTO.
     */
    PreceptorResponseDTO submitLicense(Long userId, @Valid PreceptorRequestDTO preceptorRequestDto);

    /**
     * Submits a license file for verification via multipart form upload.
     *
     * @param userId        The ID of the preceptor.
     * @param licenseNumber License identifier.
     * @param licenseState  Licensing state.
     * @param file          Uploaded license file.
     * @return Updated PreceptorResponseDTO.
     */
    PreceptorResponseDTO submitLicense(Long userId, String licenseNumber, String licenseState, MultipartFile file);

    /**
     * Reveals contact information of a preceptor.
     *
     * @param userId The ID of the preceptor.
     * @return PreceptorResponseDTO with contact details.
     */
    PreceptorContactResponseDTO revealContact(Long userId);

    /**
     * Downloads the license file of a preceptor.
     *
     * @param userId The ID of the preceptor.
     * @return Resource representing the license file.
     */
    org.springframework.core.io.Resource downloadLicense(Long userId);
}
