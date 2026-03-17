package com.digitalearn.npaxis.preceptor;

import jakarta.validation.Valid;

import java.util.List;

/**
 * Service interface for managing Preceptor details.
 */
public interface PreceptorService {

    /**
     * Retrieves all active preceptors.
     *
     * @return List of PreceptorResponseDTOs.
     */
    List<PreceptorResponseDTO> getAllActivePreceptors();

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
     * @param userId             The ID of the preceptor.
     * @param preceptorRequestDto DTO containing license information.
     * @return Updated PreceptorResponseDTO.
     */
    PreceptorResponseDTO submitLicense(Long userId, @Valid PreceptorRequestDTO preceptorRequestDto);

    /**
     * Reveals contact information of a preceptor.
     *
     * @param userId The ID of the preceptor.
     * @return PreceptorResponseDTO with contact details.
     */
    PreceptorContactResponseDTO revealContact(Long userId);
}
