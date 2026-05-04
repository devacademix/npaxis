package com.digitalearn.npaxis.messaging.exceptions;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import com.digitalearn.npaxis.exceptions.BusinessException;

/**
 * Exception thrown when an operation is attempted on a conversation in an invalid state
 * For example, sending a message to a closed conversation
 */
public class InvalidConversationStateException extends BusinessException {

    public InvalidConversationStateException(String message) {
        super(BusinessErrorCodes.BAD_REQUEST, message);
    }

    public InvalidConversationStateException(String message, Throwable cause) {
        super(BusinessErrorCodes.BAD_REQUEST, message);
        initCause(cause);
    }

    public static InvalidConversationStateException cannotSendToClosedConversation(Long conversationId) {
        return new InvalidConversationStateException(
                "Cannot send message to closed conversation " + conversationId
        );
    }

    public static InvalidConversationStateException cannotChangeStatus(String currentStatus, String targetStatus) {
        return new InvalidConversationStateException(
                "Cannot change conversation status from " + currentStatus + " to " + targetStatus
        );
    }
}


