package com.digitalearn.npaxis.exceptions;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;

/**
 * Exception thrown when a file is not found in storage.
 * This could be due to the file being deleted, moved, or never existing.
 * Maps to HTTP 404.
 */
public class StorageFileNotFoundException extends BusinessException {
    /**
     * Error code representing the file not found error.
     */
    private static final BusinessErrorCodes CODE = BusinessErrorCodes.STORAGE_FILE_NOT_FOUND;

    /**
     * Default constructor that initializes the exception with the default error code.
     */
    public StorageFileNotFoundException() {
        super(CODE);
    }

    /**
     * Constructor that initializes the exception with a custom message.
     *
     * @param message Custom message to override the default error message.
     */
    public StorageFileNotFoundException(String message) {
        super(CODE, message);
    }

    /**
     * Constructor that initializes the exception with a custom message and cause.
     *
     * @param message Custom message to override the default error message.
     * @param cause   The root cause exception.
     */
    public StorageFileNotFoundException(String message, Throwable cause) {
        super(CODE, message);
        this.initCause(cause);
    }
}

