package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin controller for managing credentials and specialties.
 * These endpoints allow admins to add, update, and remove credential and specialty options
 * that can be assigned to preceptors.
 */
@RestController
@RequestMapping("/api/admin/credentials-specialties")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Credentials & Specialties", description = "Admin APIs for managing preceptor credentials and specialties options")
public class AdminCredentialSpecialtyController {

    private final CredentialService credentialService;
    private final SpecialtyService specialtyService;
    private final CredentialRepository credentialRepository;
    private final SpecialtyRepository specialtyRepository;

    // ==================== CREDENTIAL ENDPOINTS ====================

    @Operation(
            summary = "Get all credentials",
            description = "Retrieves all credentials from the database (both predefined and user-created)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/credentials")
    public ResponseEntity<GenericApiResponse<List<CredentialDTO>>> getAllCredentials() {
        log.info("Fetching all credentials");
        List<CredentialDTO> credentials = credentialRepository.findAll().stream()
                .map(this::toCredentialDTO)
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .collect(Collectors.toList());
        return ResponseHandler.generateResponse(
                credentials,
                "Credentials fetched successfully",
                true,
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Create a new credential",
            description = "Adds a new credential option to the system"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/credentials")
    public ResponseEntity<GenericApiResponse<CredentialDTO>> createCredential(
            @Valid @RequestBody CreateCredentialRequest request) {
        log.info("Creating new credential: {}", request.name());
        Credential credential = credentialService.createPredefinedCredential(request.name(), request.description());
        return ResponseHandler.generateResponse(
                toCredentialDTO(credential),
                "Credential created successfully",
                true,
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Update a credential",
            description = "Updates an existing credential's name and description"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/credentials/{credentialId}")
    public ResponseEntity<GenericApiResponse<CredentialDTO>> updateCredential(
            @PathVariable Long credentialId,
            @Valid @RequestBody CreateCredentialRequest request) {
        log.info("Updating credential: {}", credentialId);
        Credential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + credentialId));
        credential.setName(request.name());
        credential.setDescription(request.description());
        Credential updated = credentialRepository.save(credential);
        return ResponseHandler.generateResponse(
                toCredentialDTO(updated),
                "Credential updated successfully",
                true,
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Delete a credential",
            description = "Removes a credential from the system"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/credentials/{credentialId}")
    public ResponseEntity<GenericApiResponse<Void>> deleteCredential(@PathVariable Long credentialId) {
        log.info("Deleting credential: {}", credentialId);
        if (!credentialRepository.existsById(credentialId)) {
            throw new RuntimeException("Credential not found with ID: " + credentialId);
        }
        credentialRepository.deleteById(credentialId);
        return ResponseHandler.generateResponse(
                null,
                "Credential deleted successfully",
                true,
                HttpStatus.OK
        );
    }

    // ==================== SPECIALTY ENDPOINTS ====================

    @Operation(
            summary = "Get all specialties",
            description = "Retrieves all specialties from the database (both predefined and user-created)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/specialties")
    public ResponseEntity<GenericApiResponse<List<SpecialtyDTO>>> getAllSpecialties() {
        log.info("Fetching all specialties");
        List<SpecialtyDTO> specialties = specialtyRepository.findAll().stream()
                .map(this::toSpecialtyDTO)
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .collect(Collectors.toList());
        return ResponseHandler.generateResponse(
                specialties,
                "Specialties fetched successfully",
                true,
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Create a new specialty",
            description = "Adds a new specialty option to the system"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/specialties")
    public ResponseEntity<GenericApiResponse<SpecialtyDTO>> createSpecialty(
            @Valid @RequestBody CreateSpecialtyRequest request) {
        log.info("Creating new specialty: {}", request.name());
        Specialty specialty = specialtyService.createPredefinedSpecialty(request.name(), request.description());
        return ResponseHandler.generateResponse(
                toSpecialtyDTO(specialty),
                "Specialty created successfully",
                true,
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Update a specialty",
            description = "Updates an existing specialty's name and description"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/specialties/{specialtyId}")
    public ResponseEntity<GenericApiResponse<SpecialtyDTO>> updateSpecialty(
            @PathVariable Long specialtyId,
            @Valid @RequestBody CreateSpecialtyRequest request) {
        log.info("Updating specialty: {}", specialtyId);
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new RuntimeException("Specialty not found with ID: " + specialtyId));
        specialty.setName(request.name());
        specialty.setDescription(request.description());
        Specialty updated = specialtyRepository.save(specialty);
        return ResponseHandler.generateResponse(
                toSpecialtyDTO(updated),
                "Specialty updated successfully",
                true,
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Delete a specialty",
            description = "Removes a specialty from the system"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/specialties/{specialtyId}")
    public ResponseEntity<GenericApiResponse<Void>> deleteSpecialty(@PathVariable Long specialtyId) {
        log.info("Deleting specialty: {}", specialtyId);
        if (!specialtyRepository.existsById(specialtyId)) {
            throw new RuntimeException("Specialty not found with ID: " + specialtyId);
        }
        specialtyRepository.deleteById(specialtyId);
        return ResponseHandler.generateResponse(
                null,
                "Specialty deleted successfully",
                true,
                HttpStatus.OK
        );
    }

    // ==================== HELPER METHODS ====================

    private CredentialDTO toCredentialDTO(Credential credential) {
        return new CredentialDTO(
                credential.getId(),
                credential.getName(),
                credential.getDescription(),
                credential.isPredefined()
        );
    }

    private SpecialtyDTO toSpecialtyDTO(Specialty specialty) {
        return new SpecialtyDTO(
                specialty.getId(),
                specialty.getName(),
                specialty.getDescription(),
                specialty.isPredefined()
        );
    }

    // ==================== DTOs ====================

    public record CredentialDTO(
            Long id,
            String name,
            String description,
            boolean isPredefined
    ) {
    }

    public record SpecialtyDTO(
            Long id,
            String name,
            String description,
            boolean isPredefined
    ) {
    }

    public record CreateCredentialRequest(
            String name,
            String description
    ) {
    }

    public record CreateSpecialtyRequest(
            String name,
            String description
    ) {
    }
}

