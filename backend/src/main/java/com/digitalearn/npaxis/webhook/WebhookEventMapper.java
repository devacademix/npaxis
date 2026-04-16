package com.digitalearn.npaxis.webhook;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WebhookEventMapper {

    WebhookEventResponse toResponse(WebhookProcessingEvent entity);
}

