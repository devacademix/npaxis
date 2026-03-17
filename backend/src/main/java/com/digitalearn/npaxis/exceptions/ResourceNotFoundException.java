package com.digitalearn.npaxis.exceptions;


import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;

/**
 * Exception thrown when a requested resource is not found.
 * This could be due to the resource being deleted, moved, or never existing.
 */
public class ResourceNotFoundException extends BusinessException {
    /**
     * Error code representing the resource not found error.
     */
    private static final BusinessErrorCodes CODE = BusinessErrorCodes.RESOURCE_NOT_FOUND;

    /**
     * Default constructor that initializes the exception with the default error code.
     */
    public ResourceNotFoundException() {
        super(CODE);
    }

    /**
     * Constructor that initializes the exception with a custom message.
     *
     * @param message Custom message to override the default error message.
     */
    public ResourceNotFoundException(String message) {
        super(CODE, message);
    }
}
