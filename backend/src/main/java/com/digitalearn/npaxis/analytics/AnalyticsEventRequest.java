package com.digitalearn.npaxis.analytics;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for logging an analytics event.
 */
public record AnalyticsEventRequest(

        @NotNull
        EventType eventType,

        @NotNull
        Long preceptorId

) {
}