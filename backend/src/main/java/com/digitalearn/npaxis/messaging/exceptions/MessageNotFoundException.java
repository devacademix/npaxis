package com.digitalearn.npaxis.messaging.exceptions;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import com.digitalearn.npaxis.exceptions.BusinessException;

/**
 * Exception thrown when a message is not found
 */
public class MessageNotFoundException extends BusinessException {

    public MessageNotFoundException(String message) {
        super(BusinessErrorCodes.RESOURCE_NOT_FOUND, message);
    }

    public MessageNotFoundException(String message, Throwable cause) {
        super(BusinessErrorCodes.RESOURCE_NOT_FOUND, message);
        initCause(cause);
    }

    public static MessageNotFoundException withId(Long messageId) {
        return new MessageNotFoundException("Message not found with ID: " + messageId);
    }
}


