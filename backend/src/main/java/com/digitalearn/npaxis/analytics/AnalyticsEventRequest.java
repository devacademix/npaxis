package com.digitalearn.npaxis.analytics;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalyticsEventRequest {

    @NotNull(message = "Event type requires")
    private String eventType;

    @NotNull(message = "Preceptor ID is required")
    private Long preceptorId;
}
