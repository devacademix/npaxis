package com.digitalearn.npaxis.auditing.config;

import com.digitalearn.npaxis.auditing.BaseEntity;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * JPA Entity Listener that handles soft delete operations automatically.
 * This listener intercepts delete operations and converts them to soft deletes.
 */
@Slf4j
@Component
public class SoftDeleteEntityListener {

    @PreRemove
    public void preRemove(BaseEntity entity) {
        log.info("SoftDeleteEntityListener::preRemove - Converting hard delete to soft delete.");
        entity.setDeleted(true);
        entity.setDeletedAt(LocalDateTime.now());
        CurrentUserProviderHolder.getCurrentUserId().ifPresent(entity::setDeletedBy);
    }

    @PreUpdate
    public void preUpdate(BaseEntity entity) {
        log.info("SoftDeleteEntityListener::preUpdate - Updating deletedBy field.");
        if (entity.isDeleted() && entity.getDeletedBy() == null) {
            CurrentUserProviderHolder.getCurrentUserId().ifPresent(entity::setDeletedBy);
            if (entity.getDeletedAt() == null) {
                entity.setDeletedAt(LocalDateTime.now());
            }
        }
    }
}
