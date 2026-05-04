package com.digitalearn.npaxis.messaging.conversation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Conversation entity and DTOs
 */
@Mapper(componentModel = "spring")
public abstract class ConversationMapper {

    @Mapping(target = "studentName", source = "student.displayName")
    @Mapping(target = "preceptorName", source = "preceptor.displayName")
    @Mapping(target = "unreadCount", ignore = true)
    @Mapping(target = "lastMessagePreview", ignore = true)
    public abstract ConversationResponseDTO toResponseDTO(Conversation conversation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "preceptor", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastMessageAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Conversation toEntity(ConversationRequestDTO requestDTO);

}
