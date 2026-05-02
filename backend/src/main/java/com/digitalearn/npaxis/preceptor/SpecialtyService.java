package com.digitalearn.npaxis.preceptor;

import java.util.Set;

/**
 * Service interface for managing Specialties
 */
public interface SpecialtyService {

    /**
     * Get or create a specialty by name (case-insensitive)
     * If the specialty exists with same normalized name, returns existing specialty
     * Otherwise creates a new one
     */
    Specialty getOrCreateSpecialty(String name);

    /**
     * Get all predefined specialties
     */
    Set<Specialty> getPredefinedSpecialties();

    /**
     * Create a new predefined specialty
     */
    Specialty createPredefinedSpecialty(String name, String description);

    /**
     * Find all specialties by their names (case-insensitive)
     * Creates new specialties if they don't exist
     */
    Set<Specialty> getOrCreateSpecialties(Set<String> names);
}

