package com.digitalearn.npaxis.subscription;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeConfigProperties {
    private String apiKey;
    private String webhookSecret;
    private Prices prices;

    @Data
    public static class Prices {
        private String premiumMonthly;
        private String premiumYearly;
    }
}