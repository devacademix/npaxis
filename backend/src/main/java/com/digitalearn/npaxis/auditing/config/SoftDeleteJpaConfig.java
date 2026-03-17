package com.digitalearn.npaxis.auditing.config;

import com.digitalearn.npaxis.auditing.BaseRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.digitalearn.npaxis",
        repositoryBaseClass = BaseRepositoryImpl.class
)
public class SoftDeleteJpaConfig {
}
