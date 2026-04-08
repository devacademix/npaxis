package com.digitalearn.npaxis.subscription.config;

import com.digitalearn.npaxis.subscription.StripeConfigProperties;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StripeConfiguration {

    private final StripeConfigProperties stripeConfigProperties;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeConfigProperties.getApiKey();
    }
}
