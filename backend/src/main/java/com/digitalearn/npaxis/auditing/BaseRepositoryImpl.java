package com.digitalearn.npaxis.auditing;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public class BaseRepositoryImpl<T extends BaseEntity, ID> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ID> entityInformation;

    public BaseRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }

    // Override default methods to exclude soft deleted
    @Override
    public List<T> findAll() {
        return findAllActive();
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return findAllActive(pageable);
    }

    @Override
    public Optional<T> findById(ID id) {
        return findActiveById(id);
    }

    @Override
    public boolean existsById(ID id) {
        return existsActiveById(id);
    }

    @Override
    public long count() {
        return countActive();
    }

    // Soft delete operations
    @Override
    @Transactional
    public void softDelete(ID id) {
        Assert.notNull(id, "ID must not be null!");
        Optional<T> entity = findByIdIncludingDeleted(id);
        entity.ifPresent(this::softDelete);
    }

    @Override
    @Transactional
    public void softDelete(T entity) {
        Assert.notNull(entity, "Entity must not be null!");
        if (!entity.isDeleted()) {
            entity.softDelete();
            save(entity);
        }
    }

    @Override
    @Transactional
    public void softDeleteAllById(Iterable<? extends ID> ids) {
        Assert.notNull(ids, "IDs must not be null!");
        for (ID id : ids) {
            softDelete(id);
        }
    }

    @Override
    @Transactional
    public void softDeleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "Entities must not be null!");
        for (T entity : entities) {
            softDelete(entity);
        }
    }

    @Override
    @Transactional
    public void softDeleteAll() {
        List<T> allEntities = findAllIncludingDeleted();
        softDeleteAll(allEntities);
    }

    // Find active entities
    @Override
    public List<T> findAllActive() {
        return findAll(SoftDeleteSpecifications.isNotDeleted());
    }

    @Override
    public Page<T> findAllActive(Pageable pageable) {
        return findAll(SoftDeleteSpecifications.isNotDeleted(), pageable);
    }

    @Override
    public Optional<T> findActiveById(ID id) {
        Assert.notNull(id, "ID must not be null!");
        T entity = super.findById(id).orElse(null);
        if (entity != null && entity.isActive()) {
            return Optional.of(entity);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsActiveById(ID id) {
        return findActiveById(id).isPresent();
    }

    @Override
    public long countActive() {
        return count(SoftDeleteSpecifications.isNotDeleted());
    }

    // Find including deleted entities
    @Override
    public List<T> findAllIncludingDeleted() {
        return super.findAll();
    }

    @Override
    public Page<T> findAllIncludingDeleted(Pageable pageable) {
        return super.findAll(pageable);
    }

    @Override
    public Optional<T> findByIdIncludingDeleted(ID id) {
        return super.findById(id);
    }

    @Override
    public boolean existsByIdIncludingDeleted(ID id) {
        return super.existsById(id);
    }

    @Override
    public long countIncludingDeleted() {
        return super.count();
    }

    // Find only deleted entities
    @Override
    public List<T> findAllDeleted() {
        return findAll(SoftDeleteSpecifications.isDeleted());
    }

    @Override
    public Page<T> findAllDeleted(Pageable pageable) {
        return findAll(SoftDeleteSpecifications.isDeleted(), pageable);
    }

    @Override
    public Optional<T> findDeletedById(ID id) {
        Assert.notNull(id, "ID must not be null!");
        T entity = super.findById(id).orElse(null);
        if (entity != null && entity.isDeleted()) {
            return Optional.of(entity);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsDeletedById(ID id) {
        return findDeletedById(id).isPresent();
    }

    @Override
    public long countDeleted() {
        return count(SoftDeleteSpecifications.isDeleted());
    }

    // Restore operations
    @Override
    @Transactional
    public void restore(ID id) {
        Assert.notNull(id, "ID must not be null!");
        Optional<T> entity = findDeletedById(id);
        entity.ifPresent(this::restore);
    }

    @Override
    @Transactional
    public void restore(T entity) {
        Assert.notNull(entity, "Entity must not be null!");
        if (entity.isDeleted()) {
            entity.restore();
            save(entity);
        }
    }

    @Override
    @Transactional
    public void restoreAllById(Iterable<? extends ID> ids) {
        Assert.notNull(ids, "IDs must not be null!");
        for (ID id : ids) {
            restore(id);
        }
    }

    @Override
    @Transactional
    public void restoreAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "Entities must not be null!");
        for (T entity : entities) {
            restore(entity);
        }
    }

    // Hard delete operations
    @Override
    @Transactional
    public void hardDelete(ID id) {
        Assert.notNull(id, "ID must not be null!");
        super.deleteById(id);
    }

    @Override
    @Transactional
    public void hardDelete(T entity) {
        Assert.notNull(entity, "Entity must not be null!");
        super.delete(entity);
    }

    @Override
    @Transactional
    public void hardDeleteAllById(Iterable<? extends ID> ids) {
        Assert.notNull(ids, "IDs must not be null!");
        super.deleteAllById(ids);
    }

    @Override
    @Transactional
    public void hardDeleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "Entities must not be null!");
        super.deleteAll(entities);
    }

//    @Override
//    @Transactional
//    public void hardDeleteAllDeleted() {
//        List<T> deletedEntities = findAllDeleted();
//        super.deleteAll(deletedEntities);
//    }

    // Override default delete methods to perform soft delete
    @Override
    @Transactional
    public void delete(T entity) {
        softDelete(entity);
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        softDelete(id);
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends T> entities) {
        softDeleteAll(entities);
    }

    @Override
    @Transactional
    public void deleteAllById(Iterable<? extends ID> ids) {
        softDeleteAllById(ids);
    }

    @Override
    @Transactional
    public void deleteAll() {
        softDeleteAll();
    }
}
