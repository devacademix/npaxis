package com.digitalearn.npaxis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Configuration for production deployment features:
 * - Graceful shutdown handling
 * - Character encoding filter
 * - Thread pool configuration for async operations
 */
@Slf4j
@Configuration
public class ProductionDeploymentConfig {

    /**
     * Character encoding filter to ensure UTF-8 encoding for all requests/responses.
     * Important for multilingual applications.
     */
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    /**
     * Custom ThreadPoolTaskExecutor for async operations.
     * Configured with sensible timeouts for graceful shutdown.
     */
    @Bean(name = "applicationTaskExecutor")
    @ConditionalOnProperty(name = "spring.task.execution.enabled", havingValue = "true", matchIfMissing = true)
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("ThreadPoolTaskExecutor initialized with graceful shutdown enabled");
        return executor;
    }
}
