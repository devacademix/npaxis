package com.digitalearn.npaxis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = "com.digitalearn")
public class NpaxisApplication {

    static void main(String[] args) {
        SpringApplication.run(NpaxisApplication.class, args);
    }

}
