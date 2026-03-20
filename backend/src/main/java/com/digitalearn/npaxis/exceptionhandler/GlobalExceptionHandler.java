package com.digitalearn.npaxis.exceptionhandler;

import com.digitalearn.npaxis.common.responses.DateTimeUtils;
import com.digitalearn.npaxis.exceptions.BusinessException;
import com.digitalearn.npaxis.exceptions.ResourceAlreadyExistsException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.exceptions.ValidationErrorUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.ACCOUNT_DISABLED;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.ACCOUNT_LOCKED;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.BAD_CREDENTIALS;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.BAD_REQUEST;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.CONFLICT;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.EXPIRED_TOKEN;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.FILE_TOO_LARGE;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.INSUFFICIENT_PERMISSIONS;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.INTERNAL_SERVER_ERROR;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.INVALID_TOKEN;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.JAXB_EXCEPTION_ERROR;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.NOT_FOUND;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.RESOURCE_ALREADY_EXISTS;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.UNSUPPORTED_OPERATION;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.USERNAME_NOT_FOUND;
import static com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes.VALIDATION_ERROR;

/**
 * Global exception handler for REST controllers.
 * <p>
 * This class intercepts exceptions thrown by controller methods
 * and provides standardized error responses across the application.
 * It handles:
 * <ul>
 *     <li>Authentication and Authorization errors</li>
 *     <li>Validation and constraint violations</li>
 *     <li>HL7-specific integration errors</li>
 *     <li>File upload and request parsing errors</li>
 *     <li>Unhandled system or framework exceptions</li>
 * </ul>
 * All responses include a business error code, user-friendly message,
 * and optional validation details when applicable.
 *
 * @author Molu Tyagi
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------
    // AUTHENTICATION & SECURITY
    // -------------------------------

    /**
     * Handles account status exceptions like locked or disabled accounts.
     *
     * @param ex the AccountStatusException thrown
     * @return ResponseEntity with error details and HTTP 401
     */
    @ExceptionHandler({LockedException.class, DisabledException.class})
    public ResponseEntity<ExceptionResponse> handleAccountStatus(AccountStatusException ex) {
        log.warn("Account status exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        BusinessErrorCodes code = (ex instanceof LockedException) ? ACCOUNT_LOCKED : ACCOUNT_DISABLED;
        return buildResponse(code, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles authentication failures such as bad credentials or general authentication errors.
     *
     * @param ex the Exception thrown
     * @return ResponseEntity with error details and HTTP 401
     */
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class, InternalAuthenticationServiceException.class})
    public ResponseEntity<ExceptionResponse> handleAuthExceptions(Exception ex) {
        log.warn("Authentication failure [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(BAD_CREDENTIALS, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> handleBusinessException(BusinessException ex) {

        BusinessErrorCodes error = ex.getErrorCode();

        log.warn("Business exception [{}]: {}", error.name(), ex.getMessage());

        return ResponseEntity
                .status(error.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(error.getCode())
                                .businessErrorDescription(error.getDescription())
                                .error(ex.getMessage())
                                .timestamp(String.valueOf(LocalDateTime.now()))
                                .build()
                );
    }

    /**
     * Handles the case when a username is not found during authentication.
     *
     * @param ex the UsernameNotFoundException thrown
     * @return ResponseEntity with error details and HTTP 401
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.warn("Username not found [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(USERNAME_NOT_FOUND, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles forbidden access attempts.
     *
     * @param ex the AccessDeniedException thrown
     * @return ResponseEntity with error details and HTTP 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(INSUFFICIENT_PERMISSIONS, ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles missing authentication credentials.
     *
     * @param ex the AuthenticationCredentialsNotFoundException thrown
     * @return ResponseEntity with error details and HTTP 401
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleCredentialsNotFound(AuthenticationCredentialsNotFoundException ex) {
        log.warn("Authentication credentials not found [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(INVALID_TOKEN, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // -------------------------------
    // VALIDATION ERRORS
    // -------------------------------

    /**
     * Handles validation errors for method arguments.
     *
     * @param ex the MethodArgumentNotValidException thrown
     * @return ResponseEntity with validation error details and HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        ValidationErrorUtils.ValidationErrorResult result = ValidationErrorUtils.extractValidationErrors(ex);
        ExceptionResponse response = ExceptionResponse.builder()
                .businessErrorCode(VALIDATION_ERROR.getCode())
                .businessErrorDescription(VALIDATION_ERROR.getDescription())
                .error("Request validation failed.")
                .validationErrors(result.validationErrors())
                .errors(result.fieldErrors())
                .timestamp(DateTimeUtils.localDateTimeToString(LocalDateTime.now()))
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violations from validation.
     *
     * @param ex the ConstraintViolationException thrown
     * @return ResponseEntity with validation error details and HTTP 400
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Set<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        log.warn("Constraint violation(s) [{}]: {}", ex.getClass().getSimpleName(), errors);
        ExceptionResponse response = ExceptionResponse.builder()
                .businessErrorCode(VALIDATION_ERROR.getCode())
                .businessErrorDescription(VALIDATION_ERROR.getDescription())
                .error("Constraint validation failed.")
                .validationErrors(errors)
                .timestamp(DateTimeUtils.localDateTimeToString(LocalDateTime.now()))
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles unsupported operations that are not implemented.
     *
     * @param ex the UnsupportedOperationException thrown
     * @return ResponseEntity with error details and HTTP 500
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ExceptionResponse> handleUnsupportedOperationException(UnsupportedOperationException ex) {
        log.error("Unsupported operation error [{}]: {}. Stack Trace: {}",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                Arrays.toString(ex.getStackTrace()));

        return buildResponse(UNSUPPORTED_OPERATION, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex the IllegalArgumentException thrown
     * @return ResponseEntity with error details and HTTP 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument error [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(BusinessErrorCodes.ILLEGAL_ARGUMENT, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // -------------------------------
    // REQUEST/PAYLOAD ERRORS
    // -------------------------------

    /**
     * Handles various request and payload errors such as unreadable messages, unsupported media types, etc.
     *
     * @param ex the Exception thrown
     * @return ResponseEntity with error details and HTTP 400
     */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ExceptionResponse> handleRequestExceptions(Exception ex) {
        log.warn("Request error [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(BAD_REQUEST, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles file upload errors such as exceeding maximum upload size.
     *
     * @param ex the Exception thrown
     * @return ResponseEntity with error details and HTTP 413
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public ResponseEntity<ExceptionResponse> handleFileExceptions(Exception ex) {
        log.error("File upload error [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(FILE_TOO_LARGE, ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // -------------------------------
    // JWT & SECURITY TOKEN ERRORS
    // -------------------------------

    /**
     * Handles JWT-related errors such as invalid tokens.
     *
     * @param ex the JwtException thrown
     * @return ResponseEntity with error details and HTTP 401
     */
    @ExceptionHandler({JwtException.class})
    public ResponseEntity<ExceptionResponse> handleJwtErrors(JwtException ex) {
        log.warn("JWT error [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(INVALID_TOKEN, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles JWT signature and expiration errors.
     *
     * @param ex the Exception thrown
     * @return ResponseEntity with error details and HTTP 401
     */
    @ExceptionHandler({SignatureException.class, ExpiredJwtException.class})
    public ResponseEntity<ExceptionResponse> handleJwtSignatureAndExpiration(Exception ex) {
        BusinessErrorCodes code = (ex instanceof ExpiredJwtException) ? EXPIRED_TOKEN : INVALID_TOKEN;
        log.warn("JWT signature/expiration error [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(code, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // -------------------------------
    // DATABASE & PERSISTENCE
    // -------------------------------

    /**
     * Handles data integrity violations such as duplicate entries.
     *
     * @param ex the DataIntegrityViolationException thrown
     * @return ResponseEntity with error details and HTTP 409
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Data integrity violation [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        String msg = ex.getMessage() != null && ex.getMessage().contains("Duplicate") ? "Duplicate entry detected." : "Integrity constraint violated.";
        return buildResponse(CONFLICT, msg, HttpStatus.CONFLICT);
    }

    /**
     * Handles exceptions for resources that are not found in the system.
     * This is typically triggered when a service layer cannot find an entity by its ID.
     *
     * @param ex The captured ResourceNotFoundException instance.
     * @return A standardized ResponseEntity with a 404 Not Found status.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFoundException(NoResourceFoundException ex) {
        log.error("Resource not found [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(BusinessErrorCodes.RESOURCE_NOT_FOUND, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles exceptions related to incorrect usage of the Spring Data API.
     * This is often thrown when a null entity is passed to a repository method that expects a non-null entity.
     *
     * @param ex The captured InvalidDataAccessApiUsageException instance.
     * @return A standardized ResponseEntity with a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {
        log.error("Invalid data access API usage [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(BusinessErrorCodes.INVALID_DATA_ACCESS, "Invalid data access usage: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles resource already exists errors.
     *
     * @param ex the ResourceAlreadyExistsException thrown
     * @return ResponseEntity with error details and HTTP 409
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleConflict(ResourceAlreadyExistsException ex) {
        log.warn("Resource already exists [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(RESOURCE_ALREADY_EXISTS, ex.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Handles not found errors for entities or resources.
     *
     * @param ex the Exception thrown
     * @return ResponseEntity with error details and HTTP 404
     */
    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<ExceptionResponse> handleNotFound(Exception ex) {
        log.warn("Resource not found [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(NOT_FOUND, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // -------------------------------
    // OTHER SYSTEM EXCEPTIONS
    // -------------------------------

    /**
     * Handles JSON processing errors.
     *
     * @param ex the JsonProcessingException thrown
     * @return ResponseEntity with error details and HTTP 400
     */
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ExceptionResponse> handleJsonErrors(JsonProcessingException ex) {
        log.error("JSON processing error [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(BAD_REQUEST, "Malformed JSON: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles illegal state exceptions.
     *
     * @param ex the IllegalStateException thrown
     * @return ResponseEntity with error details and HTTP 500
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state encountered [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(INTERNAL_SERVER_ERROR, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles no such element exceptions.
     *
     * @param ex the NoSuchElementException thrown
     * @return ResponseEntity with error details and HTTP 404
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ExceptionResponse> handleNoSuchElement(NoSuchElementException ex) {
        log.error("No such element found [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(NOT_FOUND, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles IO exceptions.
     *
     * @param ex the IOException thrown
     * @return ResponseEntity with error details and HTTP 500
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ExceptionResponse> handleIOException(IOException ex) {
        log.error("I/O error occurred [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(INTERNAL_SERVER_ERROR, "I/O operation failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(JAXBException.class)
    public ResponseEntity<ExceptionResponse> handleJAXBException(JAXBException ex) {
        log.error("JAXB error occurred [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildResponse(JAXB_EXCEPTION_ERROR, "JAXB processing failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // -------------------------------
    // FALLBACK HANDLER
    // -------------------------------

    /**
     * Handles all unhandled exceptions as a fallback.
     *
     * @param ex the Exception thrown
     * @return ResponseEntity with generic error details and HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception [{}]: ", ex.getClass().getSimpleName(), ex);
        return buildResponse(INTERNAL_SERVER_ERROR, "Unexpected server error. Contact admin.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Builds a standardized error response using the provided business error code, message, and HTTP status.
     *
     * @param code   Business error code from enumeration
     * @param msg    Detailed human-readable message
     * @param status HTTP status code
     * @return Standardized ResponseEntity<ExceptionResponse>
     */
    private ResponseEntity<ExceptionResponse> buildResponse(BusinessErrorCodes code, String msg, HttpStatus status) {
        ExceptionResponse response = ExceptionResponse.builder()
                .businessErrorCode(code.getCode())
                .businessErrorDescription(code.getDescription())
                .error(msg != null ? msg : code.getDescription())
                .timestamp(DateTimeUtils.localDateTimeToString(LocalDateTime.now()))
                .build();
        return new ResponseEntity<>(response, status);
    }
}
