package com.digitalearn.npaxis.exceptionhandler;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.exceptions.SubscriptionException;
import com.digitalearn.npaxis.exceptions.WebhookProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for subscription and webhook related exceptions.
 */
@RestControllerAdvice
@Slf4j
public class SubscriptionExceptionHandler {

    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleSubscriptionException(SubscriptionException ex) {
        log.error("Subscription error: {}", ex.getMessage());
        return ResponseHandler.generateResponse(
                null,
                ex.getMessage(),
                false,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(WebhookProcessingException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleWebhookProcessingException(WebhookProcessingException ex) {
        log.error("Webhook processing error: {}", ex.getMessage());
        return ResponseHandler.generateResponse(
                null,
                "Failed to process webhook event",
                false,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseHandler.generateResponse(
                null,
                ex.getMessage(),
                false,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ResponseHandler.generateResponse(
                null,
                ex.getMessage(),
                false,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseHandler.generateResponse(
                null,
                "An unexpected error occurred",
                false,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

