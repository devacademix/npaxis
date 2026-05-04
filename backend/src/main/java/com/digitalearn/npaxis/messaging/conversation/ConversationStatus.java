package com.digitalearn.npaxis.messaging.conversation;

/**
 * Status of a conversation lifecycle.
 * <p>
 * OPEN: Conversation is active and messages can be exchanged
 * RESOLVED: Issue has been resolved but conversation remains open for history
 * CLOSED: Conversation is archived, no new messages accepted
 */
public enum ConversationStatus {
    OPEN,
    RESOLVED,
    CLOSED
}
