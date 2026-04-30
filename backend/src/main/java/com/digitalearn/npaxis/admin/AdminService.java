package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    /**
     * Registers a new admin user.
     *
     * @param request admin registration request
     * @return AdminRegisterResponse with access token
     */
    AdminRegisterResponse registerAdmin(AdminRegisterRequest request);

    /**
     * Fetches all preceptors with PENDING verification status.
     *
     * @return List of pending preceptors.
     */
    Page<Preceptor> getPendingPreceptors(Pageable pageable);

    /**
     * Approves a preceptor request.
     *
     * @param userId The ID of the user (preceptor) to approve.
     * @return Success message.
     */
    String approvePreceptor(Long userId);

    /**
     * Rejects a preceptor request.
     *
     * @param userId The ID of the user (preceptor) to reject.
     * @return Success message.
     */
    String rejectPreceptor(Long userId);

    /**
     * Fetches all users with the ADMIN role.
     *
     * @return List of all admin users.
     */
    List<User> getAllAdmins();

    /**
     * Toggles a user's account state (enabled/disabled).
     *
     * @param userId  The ID of the user.
     * @param enabled The desired state.
     * @return Success message.
     */
    String toggleUserAccount(Long userId, boolean enabled);
}
