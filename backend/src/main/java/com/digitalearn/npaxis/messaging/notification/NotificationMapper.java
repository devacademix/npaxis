package com.digitalearn.npaxis.messaging.notification;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for MessageNotification entity and DTOs
 */
@Mapper(componentModel = "spring")
public abstract class NotificationMapper {

    @Mapping(target = "conversationSubject", source = "conversation.subject")
    @Mapping(target = "message", expression = "java(buildNotificationMessage(notification))")
    public abstract NotificationResponseDTO toResponseDTO(MessageNotification notification);

    /**
     * Build a human-readable notification message based on notification type
     */
    protected String buildNotificationMessage(MessageNotification notification) {
        return switch (notification.getNotificationType()) {
            case NEW_MESSAGE -> "New message from " + notification.getConversation().getPreceptor().getDisplayName();
            case REPLY -> "New reply from " + notification.getConversation().getStudent().getDisplayName();
        };
    }

}

