package com.digitalearn.npaxis.role;

import com.digitalearn.npaxis.admin.dto.RoleCreateDTO;
import com.digitalearn.npaxis.admin.dto.RoleUpdateDTO;
import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.digitalearn.npaxis.utils.APIConstants.*;


@RequiredArgsConstructor
@Slf4j
@RequestMapping(ROLES_API)
@RestController
public class RoleController {
    private final RoleService roleService;

    @Operation(summary = "Retrieve all active roles", description = "Retrieves all active roles in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active roles fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Fetching roles failed"),
            @ApiResponse(responseCode = "404", description = "Not Found - No active roles found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
    })
    @GetMapping(value = GET_ALL_ACTIVE_ROLES_API)
    public ResponseEntity<GenericApiResponse<List<RoleResponseDTO>>> getAllActiveRoles() {
        log.info("Role Controller --> Retrieve all active roles.");
        return ResponseHandler.generateResponse(roleService.getAllActiveRoles(), "Active roles fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve active role by ID", description = "Retrieves an active role by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Fetching role failed"),
            @ApiResponse(responseCode = "404", description = "Not Found - Role not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
    })
    @GetMapping(value = GET_ACTIVE_ROLE_BY_ID_API)
    public ResponseEntity<GenericApiResponse<RoleResponseDTO>> getActiveRoleById(@PathVariable Long roleId) {
        return ResponseHandler.generateResponse(this.roleService.getActiveRoleById(roleId), "Role fetched successfully", true, HttpStatus.OK);

    }

    @Operation(summary = "Create a new role (admin only)", description = "Creates a new role in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid role data"),
            @ApiResponse(responseCode = "409", description = "Conflict - Role already exists"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<GenericApiResponse<RoleResponseDTO>> createRole(
            @Valid @RequestBody RoleCreateDTO request) {
        log.info("Creating new role: {}", request.roleName());
        RoleResponseDTO role = roleService.createRole(request);
        return ResponseHandler.generateResponse(role, "Role created successfully", true, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a role (admin only)", description = "Updates an existing role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid role data"),
            @ApiResponse(responseCode = "404", description = "Not Found - Role not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/role-{roleId}")
    public ResponseEntity<GenericApiResponse<RoleResponseDTO>> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUpdateDTO request) {
        log.info("Updating role with ID: {}", roleId);
        RoleResponseDTO role = roleService.updateRole(roleId, request);
        return ResponseHandler.generateResponse(role, "Role updated successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Delete a role (admin only)", description = "Deletes an existing role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - Role not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - Role has users assigned"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/role-{roleId}")
    public ResponseEntity<GenericApiResponse<String>> deleteRole(@PathVariable Long roleId) {
        log.info("Deleting role with ID: {}", roleId);
        roleService.deleteRole(roleId);
        return ResponseHandler.generateResponse(null, "Role deleted successfully", true, HttpStatus.NO_CONTENT);
    }
}


