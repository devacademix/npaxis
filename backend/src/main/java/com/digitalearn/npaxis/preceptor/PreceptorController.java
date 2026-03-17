package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.digitalearn.npaxis.utils.APIConstants.GET_ACTIVE_PRECEPTOR_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_ALL_ACTIVE_PRECEPTORS_API;
import static com.digitalearn.npaxis.utils.APIConstants.HARD_DELETE_PRECEPTOR_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.HARD_DELETE_STUDENT_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.PRECEPTORS_API;
import static com.digitalearn.npaxis.utils.APIConstants.PUT_UPDATE_PRECEPTOR_API;
import static com.digitalearn.npaxis.utils.APIConstants.RESTORE_PRECEPTOR_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.REVEAL_CONTACT_API;
import static com.digitalearn.npaxis.utils.APIConstants.SOFT_DELETE_PRECEPTOR_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.SUBMIT_LICENSE_API;
import static com.digitalearn.npaxis.utils.APIConstants.VERIFY_PRECEPTOR_API;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping(PRECEPTORS_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Preceptor Management", description = "APIs for managing preceptor details and verification.")
public class PreceptorController {

    private final PreceptorService preceptorService;

    @Operation(summary = "Fetch all active preceptors", description = "Retrieves a list of all active preceptors.")
    @GetMapping(value = {"", "/", GET_ALL_ACTIVE_PRECEPTORS_API, GET_ALL_ACTIVE_PRECEPTORS_API + "/"})
    public ResponseEntity<Map<String, Object>> getAllActivePreceptors() {
        log.info("Fetching all active preceptors");
        List<PreceptorResponseDTO> preceptors = preceptorService.getAllActivePreceptors();
        return ResponseHandler.generateResponse(preceptors, "Preceptors fetched successfully", true, HttpStatus.OK);
    }

    @Operation(
            summary = "Fetch preceptor by ID",
            description = "Retrieves an active preceptor by their unique user ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Preceptor fetched successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PreceptorResponseDTO.class)
                            )
                    )
            }
    )
    @GetMapping(value = {GET_ACTIVE_PRECEPTOR_BY_ID_API, GET_ACTIVE_PRECEPTOR_BY_ID_API + "/"})
    public ResponseEntity<Map<String, Object>> getActivePreceptorById(@PathVariable("userId") Long userId) {
        log.info("Fetching active preceptor with ID: {}", userId);
        PreceptorResponseDTO preceptor = preceptorService.getActivePreceptorById(userId);
        return ResponseHandler.generateResponse(preceptor, "Preceptor fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Update preceptor details", description = "Updates the details of an existing preceptor.")
    @PreAuthorize("#userId == principal.userId")
    @PutMapping(value = {PUT_UPDATE_PRECEPTOR_API, PUT_UPDATE_PRECEPTOR_API + "/"})
    public ResponseEntity<Map<String, Object>> updatePreceptor(
            @PathVariable Long userId,
            @Valid @RequestBody PreceptorRequestDTO preceptorRequestDto) {
        log.info("Updating preceptor with ID: {}", userId);
        PreceptorResponseDTO updatedPreceptor = preceptorService.updatePreceptor(userId, preceptorRequestDto);
        return ResponseHandler.generateResponse(updatedPreceptor, "Preceptor updated successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Soft delete preceptor", description = "Deactivates a preceptor by their unique user ID.")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @DeleteMapping(value = {SOFT_DELETE_PRECEPTOR_BY_ID_API, SOFT_DELETE_PRECEPTOR_BY_ID_API + "/"})
    public ResponseEntity<Map<String, Object>> softDeletePreceptor(@PathVariable("userId") Long userId) {
        log.info("Soft deleting preceptor with ID: {}", userId);
        preceptorService.softDeletePreceptor(userId);
        return ResponseHandler.generateResponse(null, "Preceptor deactivated successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Hard delete preceptor", description = "Permanently deletes a preceptor by their unique user ID.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = {HARD_DELETE_PRECEPTOR_BY_ID_API, HARD_DELETE_STUDENT_BY_ID_API + "/"})
    public ResponseEntity<Map<String, Object>> hardDeletePreceptor(@PathVariable("userId") Long userId) {
        log.info("Hard deleting preceptor with ID: {}", userId);
        preceptorService.hardDeletePreceptor(userId);
        return ResponseHandler.generateResponse(null, "Preceptor deleted permanently", true, HttpStatus.OK);
    }

    @Operation(summary = "Restore preceptor", description = "Restores a soft-deleted preceptor.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = {RESTORE_PRECEPTOR_BY_ID_API, RESTORE_PRECEPTOR_BY_ID_API + "/"})
    public ResponseEntity<Map<String, Object>> restorePreceptor(@PathVariable("userId") Long userId) {
        log.info("Restoring preceptor with ID: {}", userId);
        preceptorService.restorePreceptor(userId);
        return ResponseHandler.generateResponse(null, "Preceptor restored successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Verify preceptor", description = "Verifies a preceptor (Admin action).")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = {VERIFY_PRECEPTOR_API, VERIFY_PRECEPTOR_API + "/"})
    public ResponseEntity<Map<String, Object>> verifyPreceptor(@PathVariable("userId") Long userId) {
        log.info("Verifying preceptor with ID: {}", userId);
        PreceptorResponseDTO verifiedPreceptor = preceptorService.verifyPreceptor(userId);
        return ResponseHandler.generateResponse(verifiedPreceptor, "Preceptor verified successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Submit license for verification", description = "Allows a preceptor to upload license details for verification.")
    @PreAuthorize("#userId == principal.userId")
    @PostMapping(value = {SUBMIT_LICENSE_API, SUBMIT_LICENSE_API + "/"})
    public ResponseEntity<Map<String, Object>> submitLicense(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody PreceptorRequestDTO preceptorRequestDto) {
        log.info("Submitting license for preceptor ID: {}", userId);
        PreceptorResponseDTO updatedPreceptor = preceptorService.submitLicense(userId, preceptorRequestDto);
        return ResponseHandler.generateResponse(updatedPreceptor, "License submitted for verification", true, HttpStatus.OK);
    }

    @Operation(summary = "Reveal contact information", description = "Reveals the contact details of a preceptor (premium gate).")
    @PostMapping(value = {REVEAL_CONTACT_API, REVEAL_CONTACT_API + "/"})
    public ResponseEntity<Map<String, Object>> revealContact(@PathVariable("userId") Long userId) {
        log.info("Revealing contact for preceptor ID: {}", userId);
        PreceptorContactResponseDTO preceptorContact = preceptorService.revealContact(userId);
        return ResponseHandler.generateResponse(preceptorContact, "Contact revealed successfully", true, HttpStatus.OK);
    }
}
