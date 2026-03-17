package com.digitalearn.npaxis.auditing;

import com.digitalearn.npaxis.auditing.config.SoftDeleteEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base entity class that contains common fields for all entities.
 * This class includes fields for ID, created date, updated date, and a flag for extraction status.
 * <p>
 * The class is annotated with JPA annotations to enable auditing and entity management.
 * </p>
 */
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, SoftDeleteEntityListener.class})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity {
    /**
     * Timestamp indicating when the entity was created.
     * This field is automatically populated and cannot be updated after creation.
     */
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * The user who created the entity.
     * This field is automatically populated and cannot be updated after creation.
     */
    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    /**
     * Timestamp indicating when the entity was last updated.
     * This field is automatically populated and can be updated.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;

    /**
     * The user who last updated the entity.
     * This field is automatically populated and can be updated.
     */
    @LastModifiedBy
    private Long lastModifiedBy;

    /**
     * Indicates whether the entity is deleted or not. The default value is false.
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    /**
     * Timestamp indicating when the entity was deleted.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * The user who deleted the entity.
     */
    @Column(name = "deleted_by")
    private Long deletedBy;

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    public boolean isActive() {
        return !isDeleted();
    }
}
