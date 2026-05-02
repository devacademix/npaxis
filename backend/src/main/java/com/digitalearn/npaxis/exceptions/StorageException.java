package com.digitalearn.npaxis.exceptions;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;

/**
 * Exception thrown for generic storage operation failures.
 * This is used for general storage errors that don't fit other specific categories.
 * Maps to HTTP 500.
 */
public class StorageException extends BusinessException {
    /**
     * Error code representing the generic storage error.
     */
    private static final BusinessErrorCodes CODE = BusinessErrorCodes.STORAGE_ERROR;

    /**
     * Default constructor that initializes the exception with the default error code.
     */
    public StorageException() {
        super(CODE);
    }

    /**
     * Constructor that initializes the exception with a custom message.
     *
     * @param message Custom message describing the storage error.
     */
    public StorageException(String message) {
        super(CODE, message);
    }

    /**
     * Constructor that initializes the exception with a custom message and cause.
     *
     * @param message Custom message describing the storage error.
     * @param cause   The root cause exception.
     */
    public StorageException(String message, Throwable cause) {
        super(CODE, message);
        this.initCause(cause);
    }
}

