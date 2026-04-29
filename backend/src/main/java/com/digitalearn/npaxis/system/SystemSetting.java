package com.digitalearn.npaxis.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity to store system-wide configuration settings.
 * Supports key-value pairs with encryption for sensitive data.
 */
@Entity
@Table(
        name = "system_settings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "setting_key", name = "uk_system_settings_key")
        },
        indexes = {
                @Index(name = "idx_system_settings_key", columnList = "setting_key", unique = true),
                @Index(name = "idx_system_settings_category", columnList = "category, is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingId;

    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private SettingDataType dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private SystemSettingCategory category;

    @Column(name = "is_encrypted", nullable = false)
    private boolean isEncrypted = false;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Callback to update the timestamp before any update operation
     */
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

