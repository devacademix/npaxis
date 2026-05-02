package com.digitalearn.npaxis.preceptor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Repository for managing Specialty entities
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    /**
     * Find a specialty by its normalized name (case-insensitive)
     */
    @Query("SELECT s FROM Specialty s WHERE UPPER(s.nameNormalized) = UPPER(?1)")
    Optional<Specialty> findByNameIgnoreCase(String name);

    /**
     * Find all predefined specialties
     */
    Set<Specialty> findByIsPredefinedTrue();

    /**
     * Check if a specialty exists by normalized name
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM Specialty s WHERE UPPER(s.nameNormalized) = UPPER(?1)")
    boolean existsByNameIgnoreCase(String name);
}

