package com.digitalearn.npaxis.messaging.exceptions;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import com.digitalearn.npaxis.exceptions.BusinessException;

/**
 * Exception thrown when a conversation is not found
 */
public class ConversationNotFoundException extends BusinessException {

    public ConversationNotFoundException(String message) {
        super(BusinessErrorCodes.RESOURCE_NOT_FOUND, message);
    }

    public ConversationNotFoundException(String message, Throwable cause) {
        super(BusinessErrorCodes.RESOURCE_NOT_FOUND, message);
        initCause(cause);
    }

    public static ConversationNotFoundException withId(Long conversationId) {
        return new ConversationNotFoundException("Conversation not found with ID: " + conversationId);
    }
}


