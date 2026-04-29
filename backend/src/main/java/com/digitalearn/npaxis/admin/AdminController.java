package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.digitalearn.npaxis.utils.APIConstants.ADD_ADMIN_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMINISTRATION_API;
import static com.digitalearn.npaxis.utils.APIConstants.APPROVE_PRECEPTOR_API;
import static com.digitalearn.npaxis.utils.APIConstants.BASE_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_PENDING_PRECEPTORS_API;
import static com.digitalearn.npaxis.utils.APIConstants.REJECT_PRECEPTOR_API;

@RestController
@RequestMapping(BASE_API + "/" + ADMINISTRATION_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration", description = "APIs for admin-only operations")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Add another user as admin")
    @PostMapping(value = {ADD_ADMIN_API, ADD_ADMIN_API + "/"})
    public ResponseEntity<GenericApiResponse<AdminRegisterResponse>> addAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        log.info("Admin request to add {} as ADMIN", request.email());
        AdminRegisterResponse response = adminService.registerAdmin(request);
        return ResponseHandler.generateResponse(response, "Admin added successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Get all pending preceptor verification requests")
    @GetMapping(value = {GET_PENDING_PRECEPTORS_API, GET_PENDING_PRECEPTORS_API + "/"})
    public ResponseEntity<GenericApiResponse<List<Preceptor>>> getPendingPreceptors(Pageable pageable) {
        log.info("Admin request to fetch pending preceptors");
        Page<Preceptor> preceptors = adminService.getPendingPreceptors(pageable);
        return ResponseHandler.generatePaginatedResponse(preceptors, preceptors.getContent(), "Pending preceptors fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Approve a preceptor verification request")
    @PostMapping(value = {APPROVE_PRECEPTOR_API, APPROVE_PRECEPTOR_API + "/"})
    public ResponseEntity<GenericApiResponse<String>> approvePreceptor(@PathVariable Long userId) {
        log.info("Admin request to approve preceptor with user ID {}", userId);
        String message = adminService.approvePreceptor(userId);
        return ResponseHandler.generateResponse(message, "Preceptor approved successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Reject a preceptor verification request")
    @PostMapping(value = {REJECT_PRECEPTOR_API, REJECT_PRECEPTOR_API + "/"})
    public ResponseEntity<GenericApiResponse<String>> rejectPreceptor(@PathVariable Long userId) {
        log.info("Admin request to reject preceptor with user ID {}", userId);
        String message = adminService.rejectPreceptor(userId);
        return ResponseHandler.generateResponse(message, "Preceptor rejected successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Get all admin users")
    @GetMapping(value = {"/all-admins", "/all-admins/"})
    public ResponseEntity<GenericApiResponse<List<User>>> getAllAdmins() {
        log.info("Admin request to fetch all admins");
        List<User> admins = adminService.getAllAdmins();
        return ResponseHandler.generateResponse(admins, "Admins fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Enable or disable a user account")
    @PutMapping(value = {"/user-{userId}/toggle-account", "/user-{userId}/toggle-account/"})
    public ResponseEntity<GenericApiResponse<String>> toggleUserAccount(@PathVariable Long userId, @RequestParam boolean enabled) {
        log.info("Admin request to toggle account status to {} for user ID {}", enabled, userId);
        String message = adminService.toggleUserAccount(userId, enabled);
        return ResponseHandler.generateResponse(message, "Account status updated successfully", true, HttpStatus.OK);
    }
}
