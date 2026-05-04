package com.digitalearn.npaxis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

/**
 * NPAxis Application entry point.
 * <p>
 * Automatically enables:
 * - JPA Auditing for @CreatedBy, @LastModifiedBy
 * - Async task execution for background operations
 * - Scheduled task execution for periodic jobs
 */
@Slf4j
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = "com.digitalearn")
public class NpaxisApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NpaxisApplication.class);
        Environment env = app.run(args).getEnvironment();

        logApplicationStartup(env);
    }

    /**
     * Logs application startup information safely (without exposing secrets).
     */
    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles.length == 0) {
            activeProfiles = env.getDefaultProfiles();
        }

        log.info("""
                ================================================
                NPAxis Application Started Successfully
                ================================================
                Protocol:        {}
                Server Port:     {}
                Context Path:    {}
                Active Profiles: {}
                ================================================
                """, protocol, serverPort, contextPath, Arrays.toString(activeProfiles));
    }
}
