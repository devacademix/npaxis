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
 * Implementation of SpecialtyService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final AnalyticsService analyticsService;

    @Override
    public Specialty getOrCreateSpecialty(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        // Try to find existing specialty by normalized name (case-insensitive)
        return specialtyRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    log.debug("Creating new specialty: {}", name);
                    Specialty newSpecialty = new Specialty();
                    newSpecialty.setName(name);
                    newSpecialty.setPredefined(false);
                    return specialtyRepository.save(newSpecialty);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Specialty> getPredefinedSpecialties() {
        log.debug("Fetching all predefined specialties");
        return specialtyRepository.findByIsPredefinedTrue();
    }

    @Override
    public Specialty createPredefinedSpecialty(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Specialty name cannot be blank");
        }

        // Check if already exists
        if (specialtyRepository.existsByNameIgnoreCase(name)) {
            log.warn("Specialty already exists: {}", name);
            return specialtyRepository.findByNameIgnoreCase(name).orElse(null);
        }

        log.info("Creating new predefined specialty: {}", name);
        Specialty specialty = new Specialty();
        specialty.setName(name);
        specialty.setDescription(description);
        specialty.setPredefined(true);
        Specialty savedSpecialty = specialtyRepository.save(specialty);

        // Track admin operation
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("specialtyName", name);
        metadata.put("specialtyId", savedSpecialty.getId());
        analyticsService.trackBackendEvent(
            EventType.API_CALLED,
            null,
            savedSpecialty.getId().toString(),
            metadata
        );

        return savedSpecialty;
    }

    @Override
    public Set<Specialty> getOrCreateSpecialties(Set<String> names) {
        Set<Specialty> specialties = new HashSet<>();
        if (names == null) {
            return specialties;
        }

        for (String name : names) {
            Specialty specialty = getOrCreateSpecialty(name);
            if (specialty != null) {
                specialties.add(specialty);
            }
        }

        return specialties;
    }
}

