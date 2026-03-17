package com.digitalearn.npaxis.exceptions;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import lombok.Getter;

/**
 * Base class for all business-related exceptions in the application.
 * Supports standardized error codes and custom messages.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final BusinessErrorCodes errorCode;

    /**
     * Constructs a BusinessException with the specified error code's default message.
     *
     * @param errorCode the business error code
     */
    public BusinessException(BusinessErrorCodes errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    /**
     * Constructs a BusinessException with a custom message.
     *
     * @param errorCode the business error code
     * @param message   custom message to override the default
     */
    public BusinessException(BusinessErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
