package com.digitalearn.npaxis.exceptionhandler;

import com.digitalearn.npaxis.common.responses.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Generic structure for returning error responses from exception handler.
 * Supports both HL7-specific and general business errors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY) // Skip null/empty fields for cleaner JSON output
public class ExceptionResponse {

    /**
     * Custom internal error code based on {@link BusinessErrorCodes}
     */
    private Integer businessErrorCode;

    /**
     * Human-readable description of the business error
     */
    private String businessErrorDescription;

    /**
     * Generic error message (used if no specific mapping is available)
     */
    private String error;

    /**
     * Optional: Set of validation errors (e.g., from form field constraints)
     */
    private Set<String> validationErrors;

    /**
     * Optional: Field-specific error map (e.g., "fieldName" -> "must not be blank")
     */
    private Map<String, String> errors;

    private String message;

    /**
     * Timestamp when the error occurred, in ISO or custom format.
     */
    @Builder.Default
    private String timestamp = DateTimeUtils.localDateTimeToString(LocalDateTime.now());
}
