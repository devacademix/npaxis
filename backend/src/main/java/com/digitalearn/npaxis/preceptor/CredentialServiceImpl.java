package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.analytics.AnalyticsService;
import com.digitalearn.npaxis.analytics.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private final AnalyticsService analyticsService;

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
        Credential savedCredential = credentialRepository.save(credential);

        // Track admin operation
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("credentialName", name);
        metadata.put("credentialId", savedCredential.getId());
        analyticsService.trackBackendEvent(
            EventType.API_CALLED,
            null,
            savedCredential.getId().toString(),
            metadata
        );

        return savedCredential;
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

