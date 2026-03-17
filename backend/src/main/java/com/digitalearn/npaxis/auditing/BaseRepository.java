package com.digitalearn.npaxis.auditing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    // Soft delete operations
    void softDelete(ID id);

    void softDelete(T entity);

    void softDeleteAllById(Iterable<? extends ID> ids);

    void softDeleteAll(Iterable<? extends T> entities);

    void softDeleteAll();

    // Find active entities
    List<T> findAllActive();

    Page<T> findAllActive(Pageable pageable);

    Optional<T> findActiveById(ID id);

    boolean existsActiveById(ID id);

    long countActive();

    // Find including deleted entities
    List<T> findAllIncludingDeleted();

    Page<T> findAllIncludingDeleted(Pageable pageable);

    Optional<T> findByIdIncludingDeleted(ID id);

    boolean existsByIdIncludingDeleted(ID id);

    long countIncludingDeleted();

    // Find only deleted entities
    List<T> findAllDeleted();

    Page<T> findAllDeleted(Pageable pageable);

    Optional<T> findDeletedById(ID id);

    boolean existsDeletedById(ID id);

    long countDeleted();

    // Restore operations
    void restore(ID id);

    void restore(T entity);

    void restoreAllById(Iterable<? extends ID> ids);

    void restoreAll(Iterable<? extends T> entities);

    // Hard delete operations
    void hardDelete(ID id);

    void hardDelete(T entity);

    void hardDeleteAllById(Iterable<? extends ID> ids);

    void hardDeleteAll(Iterable<? extends T> entities);

//    void hardDeleteAllDeleted();
}
