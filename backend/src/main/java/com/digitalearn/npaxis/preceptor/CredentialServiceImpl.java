package com.digitalearn.npaxis.preceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of CredentialService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CredentialServiceImpl implements CredentialService {

    private final CredentialRepository credentialRepository;

    @Override
    public Credential getOrCreateCredential(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        // Try to find existing credential by normalized name (case-insensitive)
        return credentialRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    log.debug("Creating new credential: {}", name);
                    Credential newCredential = new Credential();
                    newCredential.setName(name);
                    newCredential.setPredefined(false);
                    return credentialRepository.save(newCredential);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Credential> getPredefinedCredentials() {
        log.debug("Fetching all predefined credentials");
        return credentialRepository.findByIsPredefinedTrue();
    }

    @Override
    public Credential createPredefinedCredential(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Credential name cannot be blank");
        }

        // Check if already exists
        if (credentialRepository.existsByNameIgnoreCase(name)) {
            log.warn("Credential already exists: {}", name);
            return credentialRepository.findByNameIgnoreCase(name).orElse(null);
        }

        log.info("Creating new predefined credential: {}", name);
        Credential credential = new Credential();
        credential.setName(name);
        credential.setDescription(description);
        credential.setPredefined(true);
        return credentialRepository.save(credential);
    }

    @Override
    public Set<Credential> getOrCreateCredentials(Set<String> names) {
        Set<Credential> credentials = new HashSet<>();
        if (names == null) {
            return credentials;
        }

        for (String name : names) {
            Credential credential = getOrCreateCredential(name);
            if (credential != null) {
                credentials.add(credential);
            }
        }

        return credentials;
    }
}

