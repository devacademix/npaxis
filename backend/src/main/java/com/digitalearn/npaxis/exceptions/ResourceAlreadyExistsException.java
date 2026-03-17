package com.digitalearn.npaxis.exceptions;


import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;

/**
 * Exception thrown when there is a communication error with the HL7 system.
 * This could be due to network issues, server unavailability, or other communication-related problems.
 */
public class ResourceAlreadyExistsException extends BusinessException {
    /**
     * Error code representing the HL7 communication error.
     */
    private static final BusinessErrorCodes CODE = BusinessErrorCodes.RESOURCE_ALREADY_EXISTS;

    /**
     * Default constructor that initializes the exception with the default error code.
     */
    public ResourceAlreadyExistsException() {
        super(CODE);
    }

    /**
     * Constructor that initializes the exception with a custom message.
     *
     * @param message Custom message to override the default error message.
     */
    public ResourceAlreadyExistsException(String message) {
        super(CODE, message);
    }
}
