package com.digitalearn.npaxis.exceptions;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import lombok.Getter;

/**
 * Exception thrown when a file has an unsupported or not-permitted MIME type.
 * Maps to HTTP 415 (Unsupported Media Type).
 */
@Getter
public class StorageUnsupportedFileTypeException extends BusinessException {
    /**
     * Error code representing the unsupported file type error.
     */
    private static final BusinessErrorCodes CODE = BusinessErrorCodes.UNSUPPORTED_FILE_TYPE;

    /**
     * The MIME type that was not supported.
     */
    private final String contentType;

    /**
     * Constructor that initializes the exception with custom message and content type.
     *
     * @param message     Custom message describing the error.
     * @param contentType The MIME type that was not supported.
     */
    public StorageUnsupportedFileTypeException(String message, String contentType) {
        super(CODE, message);
        this.contentType = contentType;
    }

    /**
     * Constructor that initializes the exception with custom message, cause, and content type.
     *
     * @param message     Custom message describing the error.
     * @param cause       The root cause exception.
     * @param contentType The MIME type that was not supported.
     */
    public StorageUnsupportedFileTypeException(String message, Throwable cause, String contentType) {
        super(CODE, message);
        this.contentType = contentType;
        this.initCause(cause);
    }
}

