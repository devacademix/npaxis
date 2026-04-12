package com.digitalearn.npaxis.subscription.config;

import com.stripe.Stripe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfig {

    private final StripeProperties stripeProperties;

    @Bean
    public StripeInitializer stripeInitializer() {
        if (!StringUtils.hasText(stripeProperties.getSecretKey())) {
            throw new IllegalStateException(
                    "Stripe secret key is missing. Please set payment.stripe.secret-key."
            );
        }

        Stripe.apiKey = stripeProperties.getSecretKey();
        Stripe.setMaxNetworkRetries(stripeProperties.getMaxNetworkRetries());
        Stripe.setConnectTimeout(stripeProperties.getConnectTimeoutMs());
        Stripe.setReadTimeout(stripeProperties.getReadTimeoutMs());

        String maskedKey = maskKey(stripeProperties.getSecretKey());

        log.info(
                "Stripe SDK initialized | apiVersion={} | secretKey={} | maxRetries={} | connectTimeout={}ms | readTimeout={}ms",
                stripeProperties.getApiVersion(),
                maskedKey,
                stripeProperties.getMaxNetworkRetries(),
                stripeProperties.getConnectTimeoutMs(),
                stripeProperties.getReadTimeoutMs()
        );

        return new StripeInitializer(); // dummy bean just to enforce execution
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    static class StripeInitializer {
        // marker bean (no logic needed)
    }
}