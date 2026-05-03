package com.digitalearn.npaxis.messaging.exceptions;

import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import com.digitalearn.npaxis.exceptions.BusinessException;

/**
 * Exception thrown when a user is not authorized to access a conversation/message
 */
public class UnauthorizedParticipantException extends BusinessException {

    public UnauthorizedParticipantException(String message) {
        super(BusinessErrorCodes.INSUFFICIENT_PERMISSIONS, message);
    }

    public UnauthorizedParticipantException(String message, Throwable cause) {
        super(BusinessErrorCodes.INSUFFICIENT_PERMISSIONS, message);
        initCause(cause);
    }

    public static UnauthorizedParticipantException forConversation(Long conversationId, Long userId) {
        return new UnauthorizedParticipantException(
                "User " + userId + " is not a participant of conversation " + conversationId
        );
    }

    public static UnauthorizedParticipantException generic() {
        return new UnauthorizedParticipantException("You are not authorized to access this resource");
    }
}


