package com.digitalearn.npaxis.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.AuditorAware;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Configuration class for beans used in the application.
 * <p>
 * ============================================
 * AOP & ASYNC CONFIGURATION
 * ============================================
 *
 * @author Molu Tyagi
 * @EnableAspectJAutoProxy: - Enables Spring AOP for annotation-driven analytics tracking
 * - AnalyticsAspect intercepts @TrackEvent annotated methods
 * - proxyTargetClass=true: uses CGLIB for concrete class proxying
 * @EnableAsync: - Enables @Async method processing on separate thread pool
 * - Used for non-blocking analytics event tracking
 * - Uses configured thread pool from spring.task.execution in application.yml
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
@AllArgsConstructor
@Slf4j
public class BeansConfig {

    /**
     * Authentication provider using UserDetailsService + PasswordEncoder
     */
    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    /**
     * Password encoder used for hashing passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager provided by Spring Security
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    /**
     * Auditor provider for @CreatedBy / @LastModifiedBy
     */
    @Bean
    public AuditorAware<Long> auditorAware() {
        return new ApplicationAuditAware();
    }

    /**
     * Random generator bean
     */
    @Bean
    public Random random() {
        return new SecureRandom();
    }
}