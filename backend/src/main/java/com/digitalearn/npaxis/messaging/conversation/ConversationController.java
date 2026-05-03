package com.digitalearn.npaxis.messaging.conversation;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Conversation endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Create a new conversation (Students only)
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GenericApiResponse<ConversationResponseDTO>> createConversation(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ConversationRequestDTO requestDTO) {
        log.info("ConversationController --> Creating conversation from user ID: {}", user.getUserId());

        ConversationResponseDTO response = conversationService.createConversation(user.getUserId(), requestDTO);
        return ResponseHandler.generateResponse(
                response,
                "Conversation created successfully.",
                true,
                HttpStatus.CREATED
        );
    }

    /**
     * Get all conversations for the authenticated user
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<List<ConversationResponseDTO>>> getConversations(
            @AuthenticationPrincipal User user,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "lastMessageAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {
        log.info("ConversationController --> Fetching conversations for user ID: {}", user.getUserId());

        Page<ConversationResponseDTO> conversations = conversationService.getConversations(
                user.getUserId(),
                pageable
        );

        return ResponseHandler.generatePaginatedResponse(
                conversations,
                conversations.getContent(),
                "Conversations fetched successfully.",
                true,
                HttpStatus.OK
        );
    }

    /**
     * Get conversations filtered by status
     */
    @GetMapping("/filter-by-status")
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<List<ConversationResponseDTO>>> getConversationsByStatus(
            @AuthenticationPrincipal User user,
            @RequestParam ConversationStatus status,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "lastMessageAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {
        log.info("ConversationController --> Fetching conversations with status {} for user ID: {}",
                status, user.getUserId());

        Page<ConversationResponseDTO> conversations = conversationService.getConversationsByStatus(
                user.getUserId(),
                status,
                pageable
        );

        return ResponseHandler.generatePaginatedResponse(
                conversations,
                conversations.getContent(),
                "Conversations fetched successfully.",
                true,
                HttpStatus.OK
        );
    }

    /**
     * Get a single conversation by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<ConversationResponseDTO>> getConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        log.info("ConversationController --> Fetching conversation {} for user ID: {}", id, user.getUserId());

        ConversationResponseDTO response = conversationService.getConversation(id, user.getUserId());

        return ResponseHandler.generateResponse(
                response,
                "Conversation fetched successfully.",
                true,
                HttpStatus.OK
        );
    }

    /**
     * Update conversation status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<ConversationResponseDTO>> updateConversationStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestParam ConversationStatus status) {
        log.info("ConversationController --> Updating conversation {} status to {} for user ID: {}",
                id, status, user.getUserId());

        ConversationResponseDTO response = conversationService.updateConversationStatus(
                id,
                user.getUserId(),
                status
        );

        return ResponseHandler.generateResponse(
                response,
                "Conversation status updated successfully.",
                true,
                HttpStatus.OK
        );
    }

}



