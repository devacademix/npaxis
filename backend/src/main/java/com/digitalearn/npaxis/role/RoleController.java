package com.digitalearn.npaxis.role;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.digitalearn.npaxis.utils.APIConstants.GET_ACTIVE_ROLE_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_ALL_ACTIVE_ROLES_API;
import static com.digitalearn.npaxis.utils.APIConstants.ROLES_API;


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
    @GetMapping(value = {GET_ALL_ACTIVE_ROLES_API, GET_ALL_ACTIVE_ROLES_API + "/"})
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
    @GetMapping(value = {GET_ACTIVE_ROLE_BY_ID_API, GET_ACTIVE_ROLE_BY_ID_API + "/"})
    public ResponseEntity<GenericApiResponse<RoleResponseDTO>> getActiveRoleById(@PathVariable Long roleId) {
        return ResponseHandler.generateResponse(this.roleService.getActiveRoleById(roleId), "Role fetched successfully", true, HttpStatus.OK);

    }
}
