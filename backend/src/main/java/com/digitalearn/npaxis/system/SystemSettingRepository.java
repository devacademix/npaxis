package com.digitalearn.npaxis.system;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing SystemSetting entities.
 * Provides methods for querying and caching system configuration.
 */
@Repository
public interface SystemSettingRepository extends BaseRepository<SystemSetting, Long> {

    /**
     * Find a setting by its unique key (cached).
     */
    @Cacheable(value = "system-settings", key = "#key", unless = "#result == null")
    Optional<SystemSetting> findBySettingKeyAndIsActiveTrue(String key);

    /**
     * Find a setting by its unique key, including inactive settings.
     */
    Optional<SystemSetting> findBySettingKey(String key);

    /**
     * Find all active settings by category (cached).
     */
    @Cacheable(value = "system-settings-category", key = "#category", unless = "#result.isEmpty()")
    List<SystemSetting> findByCategoryAndIsActiveTrue(SystemSettingCategory category);

    /**
     * Find all active settings (cached).
     */
    @Cacheable(value = "system-settings-all", unless = "#result.isEmpty()")
    @Query("SELECT s FROM SystemSetting s WHERE s.isActive = true ORDER BY s.category, s.settingKey")
    List<SystemSetting> findAllActive();

    /**
     * Find all settings including inactive ones.
     */
    @Query("SELECT s FROM SystemSetting s ORDER BY s.category, s.settingKey")
    List<SystemSetting> findAllSettings();

    /**
     * Check if a setting key exists.
     */
    boolean existsBySettingKey(String key);

    /**
     * Count all active settings.
     */
    long countByIsActiveTrue();

    /**
     * Find settings by category.
     */
    List<SystemSetting> findByCategory(SystemSettingCategory category);
}

