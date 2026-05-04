package com.digitalearn.npaxis.messaging.message;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.user.User;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Message endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Send a message in a conversation
     */
    @PostMapping("/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<MessageResponseDTO>> sendMessage(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MessageRequestDTO requestDTO) {
        log.info("MessageController --> Sending message to conversation {} from user ID: {}",
                conversationId, user.getUserId());

        MessageResponseDTO response = messageService.sendMessage(
                conversationId,
                user.getUserId(),
                requestDTO
        );

        return ResponseHandler.generateResponse(
                response,
                "Message sent successfully.",
                true,
                HttpStatus.CREATED
        );
    }

    /**
     * Get all messages in a conversation
     */
    @GetMapping("/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<List<MessageResponseDTO>>> getMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User user,
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {
        log.info("MessageController --> Fetching messages for conversation {} for user ID: {}",
                conversationId, user.getUserId());

        Page<MessageResponseDTO> messages = messageService.getMessages(
                conversationId,
                user.getUserId(),
                pageable
        );

        return ResponseHandler.generatePaginatedResponse(
                messages,
                messages.getContent(),
                "Messages fetched successfully.",
                true,
                HttpStatus.OK
        );
    }

    /**
     * Mark a message as read
     */
    @PatchMapping("/{conversationId}/messages/{messageId}/read")
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<Void>> markMessageAsRead(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal User user) {
        log.info("MessageController --> Marking message {} as read for conversation {} by user ID: {}",
                messageId, conversationId, user.getUserId());

        messageService.markMessageAsRead(messageId, user.getUserId());

        return ResponseHandler.generateResponse(
                null,
                "Message marked as read.",
                true,
                HttpStatus.OK
        );
    }

    /**
     * Get unread message count for a conversation
     */
    @GetMapping("/{conversationId}/messages/unread-count")
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<Integer>> getUnreadMessageCount(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User user) {
        log.info("MessageController --> Getting unread message count for conversation {} for user ID: {}",
                conversationId, user.getUserId());

        Integer count = messageService.getUnreadMessageCount(conversationId, user.getUserId());

        return ResponseHandler.generateResponse(
                count,
                "Unread message count retrieved successfully.",
                true,
                HttpStatus.OK
        );
    }

}



