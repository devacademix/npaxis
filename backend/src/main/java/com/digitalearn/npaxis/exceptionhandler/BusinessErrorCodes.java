package com.digitalearn.npaxis.exceptionhandler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum representing standard and HL7-specific business error codes and their corresponding HTTP responses.
 * Each enum constant defines a unique error with an application-specific code,
 * an associated HTTP status, and a descriptive message.
 * <p>
 * This enum aims to centralize error definitions, making it easier to manage
 * and standardize error handling across the application, especially within
 * RESTful APIs and HL7 message processing.
 */
public enum BusinessErrorCodes {

    // ===== Common HTTP Errors =====
    /**
     * Indicates an unexpected server error.
     */
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred."),
    /**
     * Indicates that the request could not be understood or was missing required parameters.
     */
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "Invalid request payload or parameters."),
    /**
     * Indicates that authentication is required or has failed.
     */
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "Authentication required or failed."),
    /**
     * Indicates that the server understood the request but refuses to authorize it.
     */
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Access is denied."),
    /**
     * Indicates that the requested resource was not found.
     */
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Requested resource was not found."),
    /**
     * Indicates that the HTTP method used is not supported for the requested resource.
     */
    METHOD_NOT_ALLOWED(405, HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported."),
    /**
     * Indicates a conflict with the current state of the resource.
     */
    CONFLICT(409, HttpStatus.CONFLICT, "Resource conflict occurred."),
    /**
     * Indicates that the request is not acceptable based on the `Accept` headers sent by the client.
     */
    NOT_ACCEPTABLE(406, HttpStatus.NOT_ACCEPTABLE, "The request is not acceptable."),
    /**
     * Indicates that the request payload is too large.
     */
    PAYLOAD_TOO_LARGE(413, HttpStatus.PAYLOAD_TOO_LARGE, "Request payload is too large."),
    /**
     * Indicates that the media type of the request body is not supported.
     */
    UNSUPPORTED_MEDIA_TYPE(415, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type."),
    /**
     * Indicates that the requested resource is not implemented.
     */
    UNSUPPORTED_OPERATION(4003, HttpStatus.INTERNAL_SERVER_ERROR, "The operation is not supported."),

    // ===== Authentication & Authorization =====
    /**
     * Indicates that the user account is locked.
     */
    ACCOUNT_LOCKED(1001, HttpStatus.FORBIDDEN, "User account is locked."),
    /**
     * Indicates that the user account is disabled.
     */
    ACCOUNT_DISABLED(1002, HttpStatus.FORBIDDEN, "User account is disabled."),
    /**
     * Indicates that the provided username or password is invalid.
     */
    BAD_CREDENTIALS(1003, HttpStatus.UNAUTHORIZED, "Invalid username or password."),
    /**
     * Indicates that the authentication token has expired.
     */
    EXPIRED_TOKEN(1004, HttpStatus.UNAUTHORIZED, "Authentication token has expired."),
    /**
     * Indicates that the authentication token is invalid.
     */
    INVALID_TOKEN(1005, HttpStatus.UNAUTHORIZED, "Invalid authentication token."),
    /**
     * Indicates that the user does not have sufficient permissions to perform the action.
     */
    INSUFFICIENT_PERMISSIONS(1006, HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
    /**
     * Indicates that the username was not found during authentication.
     */
    USERNAME_NOT_FOUND(4003, HttpStatus.UNAUTHORIZED, "Username not found."),

    /**
     * Indicates that the User is not Email Verified yet.
     */
    EMAIL_NOT_VERIFIED(1002, HttpStatus.FORBIDDEN, "Email not verified. Verify your email first. Check your email for verification code."),


    // ===== Validation Errors =====
    /**
     * Indicates that a required field is missing from the request.
     */
    MISSING_REQUIRED_FIELD(2001, HttpStatus.BAD_REQUEST, "Required field is missing."),
    /**
     * Indicates that the input value is too short.
     */
    FIELD_TOO_SHORT(2002, HttpStatus.BAD_REQUEST, "Input is too short."),
    /**
     * Indicates that the input value is too long.
     */
    FIELD_TOO_LONG(2003, HttpStatus.BAD_REQUEST, "Input is too long."),
    /**
     * Indicates that the provided date format is invalid.
     */
    INVALID_DATE_FORMAT(2004, HttpStatus.BAD_REQUEST, "Invalid date format provided."),
    /**
     * Indicates an invalid or illegal argument was provided.
     */
    ILLEGAL_ARGUMENT(2005, HttpStatus.BAD_REQUEST, "Invalid or illegal argument."),
    /**
     * Indicates an I/O error occurred during processing.
     */
    IOEXCEPTION(2006, HttpStatus.INTERNAL_SERVER_ERROR, "An I/O error occurred."),
    // ===== User / Role Errors =====
    /**
     * Indicates that the specified user was not found.
     */
    USER_NOT_FOUND(3001, HttpStatus.NOT_FOUND, "User not found."),
    /**
     * Indicates that the specified role was not found.
     */
    ROLE_NOT_FOUND(3002, HttpStatus.NOT_FOUND, "Role not found."),
    /**
     * Indicates that the resource already exists.
     */
    RESOURCE_ALREADY_EXISTS(3003, HttpStatus.CONFLICT, "Resource already exists."),
    /**
     * Indicates that the requested resource was not found.
     */
    RESOURCE_NOT_FOUND(3004, HttpStatus.NOT_FOUND, "Resource not found."),
    INVALID_DATA_ACCESS(3005, HttpStatus.BAD_REQUEST, "Invalid data access usage, often due to incorrect API or null entity parameters."),


    // ===== Multi-Threading-Specific Errors =====
    /**
     * Indicates that the thread pool is full and cannot process new requests.
     */
    THREAD_POOL_FULL(4000, HttpStatus.SERVICE_UNAVAILABLE, "Thread pool is full, cannot process request."),
    /**
     * Indicates that a thread was interrupted during processing.
     */
    THREAD_INTERRUPTED(4001, HttpStatus.INTERNAL_SERVER_ERROR, "Thread was interrupted during processing."),
    /**
     * Indicates an error occurred while fetching data asynchronously.
     */
    ASYNC_DATA_FETCH_ERROR(4002, HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching data asynchronously."),

    /**
     * Indicates that request validation failed.
     */
    VALIDATION_ERROR(4008, HttpStatus.BAD_REQUEST, "Request validation failed."),
    /**
     * Indicates that the file size exceeds the maximum allowed limit.
     */
    FILE_TOO_LARGE(413, HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the maximum limit."),

    /**
     * Indicates an error during XML marshalling or unmarshalling.
     */
    JAXB_EXCEPTION_ERROR(4003, HttpStatus.INTERNAL_SERVER_ERROR, "Error during XML marshalling or unmarshalling."),

    // BUSINESS EXCEPTIONS
    PRECEPTOR_NOT_PREMIUM(4003, HttpStatus.BAD_REQUEST, "Can not perform this action, this Preceptor is not premium.");

    @Getter
    private final int code;

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String description;

    /**
     * Constructor for business error enum.
     *
     * @param code        Application-specific code.
     * @param httpStatus  Associated HTTP status.
     * @param description Description of the error.
     */
    BusinessErrorCodes(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.description = description;
    }
}
