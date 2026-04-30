package com.digitalearn.npaxis.admin.dto;

/**
 * DTO for system settings
 */
public record SystemSettingsDTO(
        String key,
        Object value,
        String description,
        String settingType // "GENERAL", "INTEGRATION", "NOTIFICATION", "SYSTEM_CONTROL"
) {
}

