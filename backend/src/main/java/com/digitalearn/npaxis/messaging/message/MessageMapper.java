package com.digitalearn.npaxis.messaging.message;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Message entity and DTOs
 */
@Mapper(componentModel = "spring")
public abstract class MessageMapper {

    @Mapping(target = "senderName", source = "sender.displayName")
    @Mapping(target = "senderRole", source = "senderRole")
    public abstract MessageResponseDTO toResponseDTO(Message message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "senderRole", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract Message toEntity(MessageRequestDTO requestDTO);

}

