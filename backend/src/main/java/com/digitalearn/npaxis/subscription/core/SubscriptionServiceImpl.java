package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.exceptions.SubscriptionException;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.subscription.config.StripeProperties;
import com.digitalearn.npaxis.subscription.price.SubscriptionPrice;
import com.digitalearn.npaxis.subscription.price.SubscriptionPriceRepository;
import com.digitalearn.npaxis.subscription.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service implementation for managing preceptor subscriptions
 * Handles checkout, cancellation, updates, and premium access validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final PreceptorSubscriptionRepository subscriptionRepository;
    private final SubscriptionPriceRepository priceRepository;
    private final PreceptorRepository preceptorRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;

    @Override
    public CreateCheckoutSessionResponse createCheckoutSession(Long userId, Long priceId) {
        log.info("Creating checkout session for user: {}, price: {}", userId, priceId);

        try {
            // Get preceptor
            Preceptor preceptor = preceptorRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found"));

            // Get price details
            SubscriptionPrice price = priceRepository.findById(priceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

            // Create or get Stripe customer
            String stripeCustomerId = preceptor.getStripeCustomerId();
            if (stripeCustomerId == null) {
                Customer customer = stripeClient.createCustomer(preceptor.getEmail());
                stripeCustomerId = customer.getId();
                preceptor.setStripeCustomerId(stripeCustomerId);
                preceptorRepository.save(preceptor);
                log.info("Created new Stripe customer: {} for preceptor: {}", stripeCustomerId, userId);
            }

            // Create checkout session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(stripeCustomerId)
                    .setSuccessUrl(stripeProperties.getSuccessUrl())
                    .setCancelUrl(stripeProperties.getCancelUrl())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(price.getStripePriceId())
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            Session session = stripeClient.createCheckoutSession(params);
            log.info("Checkout session created successfully: {}", session.getId());

            return new CreateCheckoutSessionResponse(
                    session.getId(),
                    session.getUrl(),
                    session.getCustomer()
            );

        } catch (StripeException e) {
            log.error("Stripe error while creating checkout session", e);
            throw new SubscriptionException("Failed to create checkout session: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDetailResponse getSubscriptionDetail(Long userId) {
        log.info("Fetching subscription details for user: {}", userId);

        PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        return subscriptionMapper.toDetailResponse(subscription);
    }

    @Override
    public void cancelSubscription(Long userId) {
        log.info("Canceling subscription for user: {}", userId);

        try {
            PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

            // Cancel at period end (not immediate)
            Subscription stripeSubscription = stripeClient.cancelSubscription(
                    subscription.getStripeSubscriptionId()
            );

            // Update local subscription record
            subscription.setCancelAtPeriodEnd(true);
            subscription.setCanceledAt(LocalDateTime.now());
            subscription.setCanceledReason("User requested cancellation");
            subscriptionRepository.save(subscription);

            log.info("Subscription canceled at period end for user: {}", userId);

        } catch (StripeException e) {
            log.error("Stripe error while canceling subscription", e);
            throw new SubscriptionException("Failed to cancel subscription: " + e.getMessage());
        }
    }

    @Override
    public void updateSubscription(Long userId, UpdateSubscriptionRequest request) {
        log.info("Updating subscription for user: {} with price: {}", userId, request.priceId());

        try {
            PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

            SubscriptionPrice newPrice = priceRepository.findById(request.priceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

            // Update in Stripe
            Subscription stripeSubscription = stripeClient.updateSubscription(
                    subscription.getStripeSubscriptionId(),
                    newPrice.getStripePriceId()
            );

            // Update local subscription record
            subscription.setPrice(newPrice);
            subscriptionRepository.save(subscription);

            log.info("Subscription updated successfully for user: {}", userId);

        } catch (StripeException e) {
            log.error("Stripe error while updating subscription", e);
            throw new SubscriptionException("Failed to update subscription: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionHistoryResponse> getSubscriptionHistory(Long userId, Pageable pageable) {
        log.info("Fetching subscription history for user: {}", userId);

        Page<PreceptorSubscription> history = subscriptionRepository
                .findByPreceptor_UserIdOrderByCreatedAtDesc(userId, pageable);

        return history.map(subscriptionMapper::toHistoryResponse);
    }

    @Override
    public String createCustomerPortal(Long userId) {
        log.info("Creating customer portal for user: {}", userId);

        try {
            Preceptor preceptor = preceptorRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found"));

            PreceptorSubscription subscription = subscriptionRepository.findByPreceptor_UserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

            if (preceptor.getStripeCustomerId() == null) {
                throw new SubscriptionException("Stripe customer not found");
            }

            // Create customer portal session
            com.stripe.param.billingportal.SessionCreateParams params =
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(preceptor.getStripeCustomerId())
                            .setReturnUrl(stripeProperties.getCustomerPortalReturnUrl())
                            .build();

            com.stripe.model.billingportal.Session session = stripeClient.createCustomerPortal(params);
            log.info("Customer portal created successfully for user: {}", userId);

            return session.getUrl();

        } catch (StripeException e) {
            log.error("Stripe error while creating customer portal", e);
            throw new SubscriptionException("Failed to create customer portal: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessPremiumFeatures(Long userId) {
        log.debug("Checking premium access for user: {}", userId);

        Optional<PreceptorSubscription> subscription = subscriptionRepository.findByPreceptor_UserId(userId);

        if (subscription.isEmpty()) {
            return false;
        }

        PreceptorSubscription sub = subscription.get();
        LocalDateTime now = LocalDateTime.now();

        // Active or trialing subscriptions have access
        if (sub.getStatus() == SubscriptionStatus.ACTIVE || sub.getStatus() == SubscriptionStatus.TRIALING) {
            return true;
        }

        // Canceled subscriptions have access until period end (grace period)
        if (sub.getStatus() == SubscriptionStatus.CANCELED &&
                sub.getCurrentPeriodEnd() != null &&
                sub.getCurrentPeriodEnd().isAfter(now)) {
            return true;
        }

        return false;
    }

     @Override
     public void syncLocalSubscriptionFromStripe(String stripeSubscriptionId) {
         log.info("Syncing subscription from Stripe: {}", stripeSubscriptionId);

         try {
             Subscription stripeSubscription = stripeClient.retrieveSubscription(stripeSubscriptionId);

             Optional<PreceptorSubscription> existingSubscription =
                     subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);

             PreceptorSubscription subscription;

             if (existingSubscription.isEmpty()) {
                 // Create new subscription if it doesn't exist
                 log.info("Creating new local subscription for Stripe subscription: {}", stripeSubscriptionId);

                 // Get preceptor by stripe customer ID
                 String customerId = stripeSubscription.getCustomer();
                 Preceptor preceptor = preceptorRepository.findByStripeCustomerId(customerId)
                         .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found for customer: " + customerId));

                 // Get the first price from the subscription items
                 if (stripeSubscription.getItems().getData().isEmpty()) {
                     throw new SubscriptionException("No items found in Stripe subscription: " + stripeSubscriptionId);
                 }

                 String stripePriceId = stripeSubscription.getItems().getData().get(0).getPrice().getId();

                 // Find price in local database
                 SubscriptionPrice price = priceRepository.findByStripePriceId(stripePriceId)
                         .orElseThrow(() -> new ResourceNotFoundException("Price not found for Stripe price: " + stripePriceId));

                 // Create new subscription record
                 subscription = PreceptorSubscription.builder()
                         .preceptor(preceptor)
                         .plan(price.getPlan())
                         .price(price)
                         .stripeCustomerId(customerId)
                         .stripeSubscriptionId(stripeSubscriptionId)
                         .status(mapStripeStatusToLocal(stripeSubscription.getStatus()))
                         .cancelAtPeriodEnd(stripeSubscription.getCancelAtPeriodEnd())
                         .accessEnabled(true)
                         .paymentRetryCount(0)
                         .build();

                 log.info("Created new PreceptorSubscription for preceptor: {} with status: {}",
                         preceptor.getUserId(), subscription.getStatus());

             } else {
                 // Update existing subscription
                 subscription = existingSubscription.get();
                 log.info("Updating existing subscription for Stripe subscription: {}", stripeSubscriptionId);
             }

             // Update status
             String status = stripeSubscription.getStatus();
             subscription.setStatus(mapStripeStatusToLocal(status));

              // Update cancel at period end flag
              subscription.setCancelAtPeriodEnd(stripeSubscription.getCancelAtPeriodEnd());

              // Update period dates - extract timestamps from Stripe subscription using reflection
              Long currentPeriodStart = null;
              Long currentPeriodEnd = null;

              try {
                  Object rawJson = stripeSubscription.getRawJsonObject();
                  if (rawJson != null) {
                      // Try to extract current_period_start
                      Object startValue = rawJson.getClass().getMethod("get", String.class).invoke(rawJson, "current_period_start");
                      if (startValue != null) {
                          currentPeriodStart = extractLongFromObject(startValue);
                      }

                      // Try to extract current_period_end
                      Object endValue = rawJson.getClass().getMethod("get", String.class).invoke(rawJson, "current_period_end");
                      if (endValue != null) {
                          currentPeriodEnd = extractLongFromObject(endValue);
                      }
                  }
                  log.debug("Raw extraction: start={}, end={}", currentPeriodStart, currentPeriodEnd);
              } catch (Exception e) {
                  log.warn("Error extracting period dates from raw JSON: {}", e.getMessage());
              }

              if (currentPeriodStart != null && currentPeriodStart > 0) {
                  subscription.setCurrentPeriodStart(
                          LocalDateTime.ofInstant(
                                  java.time.Instant.ofEpochSecond(currentPeriodStart),
                                  java.time.ZoneId.systemDefault()
                          )
                  );
                  log.info("✓ Set current period start: {} for subscription: {}", currentPeriodStart, stripeSubscriptionId);
              } else {
                  log.warn("⚠ Could not get current_period_start for subscription: {}", stripeSubscriptionId);
              }

              if (currentPeriodEnd != null && currentPeriodEnd > 0) {
                  subscription.setCurrentPeriodEnd(
                          LocalDateTime.ofInstant(
                                  java.time.Instant.ofEpochSecond(currentPeriodEnd),
                                  java.time.ZoneId.systemDefault()
                          )
                  );
                  log.info("✓ Set current period end: {} for subscription: {}", currentPeriodEnd, stripeSubscriptionId);
              } else {
                  log.warn("⚠ Could not get current_period_end for subscription: {}", stripeSubscriptionId);
              }

              // Set next billing date (same as period end)
              if (currentPeriodEnd != null && currentPeriodEnd > 0) {
                  subscription.setNextBillingDate(
                          LocalDateTime.ofInstant(
                                  java.time.Instant.ofEpochSecond(currentPeriodEnd),
                                  java.time.ZoneId.systemDefault()
                          )
                  );
                  log.info("✓ Set next billing date: {} for subscription: {}", currentPeriodEnd, stripeSubscriptionId);
              } else {
                  log.warn("⚠ Could not get next billing date for subscription: {}", stripeSubscriptionId);
              }

             subscriptionRepository.save(subscription);
             log.info("✓ SYNCED PreceptorSubscription: {} - currentPeriodStart: {}, currentPeriodEnd: {}, nextBillingDate: {}",
                     stripeSubscriptionId, subscription.getCurrentPeriodStart(),
                     subscription.getCurrentPeriodEnd(), subscription.getNextBillingDate());

         } catch (StripeException e) {
             log.error("Stripe error while syncing subscription", e);
             throw new SubscriptionException("Failed to sync subscription: " + e.getMessage());
         } catch (ResourceNotFoundException | SubscriptionException e) {
             log.error("Error syncing subscription: {}", e.getMessage());
             throw e;
         }
     }

      /**
       * Extract a long value from Stripe subscription using reflection (handles GSON JsonObject)
       */
      @SuppressWarnings("all")
      private Long extractLongFromSubscription(Subscription subscription, String fieldName) {
          try {
              java.lang.reflect.Method getRawMethod = subscription.getClass().getMethod("getRawJsonObject");
              Object rawJson = getRawMethod.invoke(subscription);

              if (rawJson != null) {
                  try {
                      java.lang.reflect.Method getMethod = rawJson.getClass().getMethod("get", String.class);
                      Object value = getMethod.invoke(rawJson, fieldName);
                      if (value instanceof Number) {
                          return ((Number) value).longValue();
                      } else if (value != null) {
                          try {
                              return Long.parseLong(value.toString());
                          } catch (NumberFormatException e) {
                              log.debug("Could not parse {} as long: {}", fieldName, value);
                          }
                      }
                  } catch (Exception e) {
                      log.debug("Could not extract {} from subscription: {}", fieldName, e.getMessage());
                  }
              }
          } catch (Exception e) {
              log.debug("Could not access raw JSON from subscription: {}", e.getMessage());
          }
          return null;
      }

      /**
       * Extract a long value from Object (handles both Number and String representations)
       */
      private Long extractLongFromObject(Object value) {
          if (value == null) {
              return null;
          }

          // Direct Number instance
          if (value instanceof Number) {
              return ((Number) value).longValue();
          }

          // Try to convert to string and parse
          try {
              String stringValue = value.toString().trim();
              if (stringValue.isEmpty() || "null".equalsIgnoreCase(stringValue)) {
                  return null;
              }

              // Remove quotes if present (for GSON JsonPrimitive)
              if (stringValue.startsWith("\"") && stringValue.endsWith("\"")) {
                  stringValue = stringValue.substring(1, stringValue.length() - 1);
              }

              return Long.parseLong(stringValue);
          } catch (NumberFormatException e) {
              log.debug("Could not parse value as long: {}", value);
              return null;
          }
      }

     /**
      * Map Stripe subscription status to local status enum
      */
     private SubscriptionStatus mapStripeStatusToLocal(String stripeStatus) {
         return switch (stripeStatus) {
             case "trialing" -> SubscriptionStatus.TRIALING;
             case "active" -> SubscriptionStatus.ACTIVE;
             case "past_due" -> SubscriptionStatus.PAST_DUE;
             case "canceled" -> SubscriptionStatus.CANCELED;
             case "incomplete" -> SubscriptionStatus.INCOMPLETE;
             case "unpaid" -> SubscriptionStatus.UNPAID;
             default -> {
                 log.warn("Unknown Stripe status: {}", stripeStatus);
                 yield SubscriptionStatus.INCOMPLETE;
             }
         };
     }
}











