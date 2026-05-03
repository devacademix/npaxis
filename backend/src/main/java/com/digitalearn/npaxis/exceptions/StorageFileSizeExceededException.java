package com.digitalearn.npaxis.exceptions;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import lombok.Getter;

/**
 * Exception thrown when a file exceeds the maximum allowed size during upload.
 * Maps to HTTP 400.
 */
@Getter
public class StorageFileSizeExceededException extends BusinessException {
    /**
     * Error code representing the file size exceeded error.
     */
    private static final BusinessErrorCodes CODE = BusinessErrorCodes.FILE_SIZE_EXCEEDED;

    /**
     * The actual size of the file in bytes.
     */
    private final long fileSize;

    /**
     * The maximum allowed file size in bytes.
     */
    private final long maxAllowed;

    /**
     * Constructor that initializes the exception with custom message, file size, and max allowed size.
     *
     * @param message    Custom message describing the error.
     * @param fileSize   The actual size of the file in bytes.
     * @param maxAllowed The maximum allowed file size in bytes.
     */
    public StorageFileSizeExceededException(String message, long fileSize, long maxAllowed) {
        super(CODE, message);
        this.fileSize = fileSize;
        this.maxAllowed = maxAllowed;
    }

    /**
     * Constructor that initializes the exception with custom message, cause, file size, and max allowed size.
     *
     * @param message    Custom message describing the error.
     * @param cause      The root cause exception.
     * @param fileSize   The actual size of the file in bytes.
     * @param maxAllowed The maximum allowed file size in bytes.
     */
    public StorageFileSizeExceededException(String message, Throwable cause, long fileSize, long maxAllowed) {
        super(CODE, message);
        this.fileSize = fileSize;
        this.maxAllowed = maxAllowed;
        this.initCause(cause);
    }
}

