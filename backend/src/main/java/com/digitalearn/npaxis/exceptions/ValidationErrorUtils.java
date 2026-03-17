package com.digitalearn.npaxis.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for extracting and formatting validation errors.
 */
@Slf4j
public class ValidationErrorUtils {

    /**
     * Extracts field-level and global validation errors from MethodArgumentNotValidException.
     *
     * @param ex the validation exception
     * @return a map containing two parts:
     * <ul>
     *   <li><b>fieldErrors</b>: <code>Map&lt;String, String&gt;</code> of field to error message</li>
     *   <li><b>validationErrors</b>: <code>Set&lt;String&gt;</code> of general/global validation messages</li>
     * </ul>
     */
    public static ValidationErrorResult extractValidationErrors(MethodArgumentNotValidException ex) {
        // Initialize maps to hold field and global errors
        log.info("Number of field errors: {}", ex.getBindingResult().getFieldErrors().size());
        Map<String, String> fieldErrors = new HashMap<>();
        Set<String> globalErrors = new HashSet<>();

        // Iterate through field errors and add them to the map
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            log.info("Field error: {} - {}", error.getField(), error.getDefaultMessage());
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        // Iterate through global errors and add them to the set
        log.info("Number of global errors: {}", ex.getBindingResult().getGlobalErrors().size());
        ex.getBindingResult().getGlobalErrors().forEach(globalError ->
                globalErrors.add(globalError.getDefaultMessage())
        );

        // Log the global errors
        globalErrors.forEach(globalError -> log.info("Global error: {}", globalError));
        return new ValidationErrorResult(fieldErrors, globalErrors);
    }

    /**
     * A record to return both field and global validation errors.
     *
     * @param fieldErrors      a map of field names to their error messages
     * @param validationErrors a set of general or global validation error messages
     */
    public record ValidationErrorResult(Map<String, String> fieldErrors, Set<String> validationErrors) {
    }
}
