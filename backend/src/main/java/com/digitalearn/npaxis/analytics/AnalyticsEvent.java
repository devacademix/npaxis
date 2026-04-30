package com.digitalearn.npaxis.analytics;

import com.digitalearn.npaxis.auditing.BaseEntity;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * Entity representing analytics events in the system.
 */
@Entity
@Table(
        name = "analytics_events",
        indexes = {
                @Index(name = "idx_event_type", columnList = "event_type"),
                @Index(name = "idx_preceptor", columnList = "preceptor_id"),
                @Index(name = "idx_user", columnList = "user_id"),
                @Index(name = "idx_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AnalyticsEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analyticsId;

    /**
     * Type of event (PROFILE_VIEW, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    /**
     * Associated preceptor.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preceptor_id")
    private Preceptor preceptor;

    /**
     * User who triggered the event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Flexible metadata (JSONB)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}