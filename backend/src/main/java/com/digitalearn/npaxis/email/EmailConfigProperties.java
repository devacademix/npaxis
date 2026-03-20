package com.digitalearn.npaxis.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "npaxis.email")
public class EmailConfigProperties {
    private String fromAddress;
    private String fromName;
}