package com.digitalearn.npaxis.preceptor;

import java.util.Set;

/**
 * Service interface for managing Credentials
 */
public interface CredentialService {

    /**
     * Get or create a credential by name (case-insensitive)
     * If the credential exists with same normalized name, returns existing credential
     * Otherwise creates a new one
     */
    Credential getOrCreateCredential(String name);

    /**
     * Get all predefined credentials
     */
    Set<Credential> getPredefinedCredentials();

    /**
     * Create a new predefined credential
     */
    Credential createPredefinedCredential(String name, String description);

    /**
     * Find all credentials by their names (case-insensitive)
     * Creates new credentials if they don't exist
     */
    Set<Credential> getOrCreateCredentials(Set<String> names);
}

