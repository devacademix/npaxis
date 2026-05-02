package com.digitalearn.npaxis.preceptor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Repository for managing Credential entities
 */
@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    /**
     * Find a credential by its normalized name (case-insensitive)
     */
    @Query("SELECT c FROM Credential c WHERE UPPER(c.nameNormalized) = UPPER(?1)")
    Optional<Credential> findByNameIgnoreCase(String name);

    /**
     * Find all predefined credentials
     */
    Set<Credential> findByIsPredefinedTrue();

    /**
     * Check if a credential exists by normalized name
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Credential c WHERE UPPER(c.nameNormalized) = UPPER(?1)")
    boolean existsByNameIgnoreCase(String name);
}

