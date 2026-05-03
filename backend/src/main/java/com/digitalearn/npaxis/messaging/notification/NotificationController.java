package com.digitalearn.npaxis.messaging.notification;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Notification endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for the authenticated user
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<List<NotificationResponseDTO>>> getNotifications(
            @AuthenticationPrincipal User user,
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {
        log.info("NotificationController --> Fetching notifications for user ID: {}", user.getUserId());

        Page<NotificationResponseDTO> notifications = notificationService.getNotifications(
                user.getUserId(),
                pageable
        );

        return ResponseHandler.generatePaginatedResponse(
                notifications,
                notifications.getContent(),
                "Notifications fetched successfully.",
                true,
                HttpStatus.OK
        );
    }

    /**
     * Mark a notification as read
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<Void>> markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        log.info("NotificationController --> Marking notification {} as read for user ID: {}", id, user.getUserId());

        notificationService.markNotificationAsRead(id, user.getUserId());

        return ResponseHandler.generateResponse(
                null,
                "Notification marked as read.",
                true,
                HttpStatus.OK
        );
    }

    /**
     * Get unread notification count for the user
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<Integer>> getUnreadNotificationCount(
            @AuthenticationPrincipal User user) {
        log.info("NotificationController --> Getting unread notification count for user ID: {}", user.getUserId());

        Integer count = notificationService.getUnreadNotificationCount(user.getUserId());

        return ResponseHandler.generateResponse(
                count,
                "Unread notification count retrieved successfully.",
                true,
                HttpStatus.OK
        );
    }

}



